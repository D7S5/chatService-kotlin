package com.example.chatService.security

import com.example.chatService.dto.JwtResponse
import com.example.chatService.dto.LoginRequest
import com.example.chatService.dto.LoginResponse
import com.example.chatService.dto.RegisterRequest
import com.example.chatService.dto.UserDto
import com.example.chatService.entity.AuthProvider
import com.example.chatService.entity.Role
import com.example.chatService.entity.User
import com.example.chatService.repository.UserRepository
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Service
@Transactional
class AuthService(
        private val authenticationManager: AuthenticationManager,
        private val jwtTokenProvider: JwtTokenProvider,
        private val userRepository: UserRepository,
        private val passwordEncoder: PasswordEncoder,
        private val redisTemplate: RedisTemplate<String, String>,
        private val cookieUtil: CookieUtil
) {

    @Value("\${jwt.refresh-token-expiry}")
    private var jwtRefreshTokenExpiry: Long = 0

    companion object {
        private const val REDIS_CURRENT_PREFIX = "RT:current:"
        private const val REDIS_BLACKLIST_PREFIX = "RT:blacklist:"
    }

    fun login(
            request: LoginRequest,
            response: HttpServletResponse
    ): LoginResponse {
        val user = userRepository.findByEmail(request.email())
                .orElseThrow { UsernameNotFoundException("User not found") }

        if (user.provider != AuthProvider.LOCAL) {
            throw IllegalArgumentException("소셜 로그인으로 가입한 계정입니다. 해당 소셜 로그인을 이용해주세요.")
        }

        if (user.passwordValue.isNullOrBlank()) {
            throw IllegalArgumentException("비밀번호가 없는 계정입니다.")
        }

        val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        )

        SecurityContextHolder.getContext().authentication = authentication

        val principal = authentication.principal as UserPrincipal

        val loginUser = userRepository.findByEmail(principal.name)
                .orElseThrow { UsernameNotFoundException("User not found") }

        val accessToken = jwtTokenProvider.generateAccessToken(loginUser)
        val refreshToken = jwtTokenProvider.generateRefreshToken(loginUser)

        val hash = TokenHashUtil.hash(refreshToken)

        redisTemplate.opsForValue().set(
                REDIS_CURRENT_PREFIX + loginUser.id,
                hash,
                jwtRefreshTokenExpiry,
                TimeUnit.MILLISECONDS
        )

        cookieUtil.addRefreshTokenCookie(response, refreshToken)

        return LoginResponse(
                accessToken,
                UserDto.from(loginUser)
        )
    }

    fun refresh(
            request: HttpServletRequest,
            response: HttpServletResponse
    ): JwtResponse {
        val oldRefreshToken = cookieUtil.getRefreshToken(request)

        if (oldRefreshToken == null || oldRefreshToken.isBlank()) {
            throw RuntimeException("Refresh Token missing")
        }

        if (!jwtTokenProvider.validateToken(oldRefreshToken)) {
            throw RuntimeException("invalid Refresh Token")
        }

        val userId = jwtTokenProvider.getSubject(oldRefreshToken)
        val oldHash = TokenHashUtil.hash(oldRefreshToken)

        if (redisTemplate.hasKey(REDIS_BLACKLIST_PREFIX + oldHash)) {
            redisTemplate.delete(REDIS_CURRENT_PREFIX + userId)
            cookieUtil.clearRefreshTokenCookie(response)
            throw SecurityException("Refresh Token Reuse Detected")
        }

        val key = REDIS_CURRENT_PREFIX + userId
        val savedHash = redisTemplate.opsForValue().get(key)

        if (oldHash != savedHash) {
            redisTemplate.delete(key)
            cookieUtil.clearRefreshTokenCookie(response)
            throw SecurityException("Refresh token Mismatch")
        }

        redisTemplate.opsForValue().set(
                REDIS_BLACKLIST_PREFIX + oldHash,
                "USED",
                remainingTTL(oldRefreshToken),
                TimeUnit.MILLISECONDS
        )

        val user = userRepository.findById(userId)
                .orElseThrow { RuntimeException("User not found") }

        val newAccessToken = jwtTokenProvider.generateAccessToken(user)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(user)
        val newHash = TokenHashUtil.hash(newRefreshToken)

        redisTemplate.opsForValue().set(
                REDIS_CURRENT_PREFIX + userId,
                newHash,
                jwtRefreshTokenExpiry,
                TimeUnit.MILLISECONDS
        )

        cookieUtil.addRefreshTokenCookie(response, newRefreshToken)

        return JwtResponse(newAccessToken)
    }

    fun logout(
            response: HttpServletResponse,
            authentication: Authentication?,
            request: HttpServletRequest
    ) {
        var userId: String? = null

        if (authentication != null && authentication.principal is UserPrincipal) {
            val principal = authentication.principal as UserPrincipal
            userId = principal.id
        }

        if (userId == null) {
            userId = cookieUtil.tryResolveUserFromRefreshCookie(request)

            if (userId == null) {
                println("Cannot resolve user From refresh cookie")
                cookieUtil.clearRefreshTokenCookie(response)
                return
            }
        }

        redisTemplate.delete(REDIS_CURRENT_PREFIX + userId)
        cookieUtil.clearRefreshTokenCookie(response)
    }

    fun register(@Valid request: RegisterRequest) {
        if (userRepository.existsByUsernameValue(request.username())) {
            throw IllegalArgumentException("이미 존재하는 닉네임입니다")
        }

        if (userRepository.existsByEmail(request.email())) {
            throw IllegalArgumentException("이미 존재하는 이메일입니다")
        }

        val user = User(
                usernameValue = request.username(),
                passwordValue = passwordEncoder.encode(request.password()),
                email = request.email(),
                role = Role.USER,
                online = false,
                provider = AuthProvider.LOCAL,
                providerId = null
        )

        userRepository.save(user)
    }

    private fun remainingTTL(refreshToken: String): Long {
        val claims: Claims = Jwts.parser()
                .verifyWith(jwtTokenProvider.getKey())
                .build()
                .parseSignedClaims(refreshToken)
                .payload

        val expirationTime = claims.expiration.time
        val now = System.currentTimeMillis()

        return maxOf(expirationTime - now, 0)
    }
}
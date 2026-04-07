package com.example.chatService.oauth

import com.example.chatService.repository.UserRepository
import com.example.chatService.security.CookieUtil
import com.example.chatService.security.JwtTokenProvider
import com.example.chatService.security.TokenHashUtil
import com.example.chatService.security.UserPrincipal
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.web.exchanges.HttpExchange.Principal
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RestController
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

@Component
class OAuth2SuccessHandler (
        private val jwtTokenProvider: JwtTokenProvider,
        private val userRepository: UserRepository,
        private val cookieUtil: CookieUtil,

        private val redisTemplate: StringRedisTemplate,

        @Value("\${jwt.refresh-token-expiry}")
        private val jwtRefreshTokenExpiry : Long,
        @Value("\${app.frontend-url}")
        private val frontendUrl: String
) : SimpleUrlAuthenticationSuccessHandler() {
    companion object {
        private const val REDIS_CURRENT_PREFIX = "RT:current:";
    }
    @Throws(IOException::class)
    override fun onAuthenticationSuccess(request: HttpServletRequest,
                                         response: HttpServletResponse,
                                         authentication: Authentication) {
        val principal = authentication.principal as UserPrincipal

        val user = userRepository.findById(principal.id).orElseThrow()

        val accessToken = jwtTokenProvider.generateAccessToken(user)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user)

        val hash = TokenHashUtil.hash(refreshToken)

        redisTemplate.opsForValue().set(
                REDIS_CURRENT_PREFIX + user.id,
                hash,
                jwtRefreshTokenExpiry,
                TimeUnit.MILLISECONDS
        )

        cookieUtil.addRefreshTokenCookie(response, refreshToken)

        val encoded = URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
        val redirectUrl = if (!user.nicknameCompleted) {
            "$frontendUrl/oauth/nickname?token=$encoded"
        } else  {
            "$frontendUrl/oauth/success?token=$encoded"
        }

        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, redirectUrl)
    }
}
package com.example.chatService.security

import com.example.chatService.entity.User
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtConfig: JwtConfig
) {
    private val log = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    private lateinit var key: SecretKey

    @PostConstruct
    fun init() {
        val rawSecret = jwtConfig.secret
            ?: throw IllegalStateException("JWT secret is missing! Set 'jwt.secret' in application.yml or environment variable.")

        if (rawSecret.isBlank()) {
            throw IllegalStateException("JWT secret is missing! Set 'jwt.secret' in application.yml or environment variable.")
        }

        val trimmedSecret = rawSecret.trim()
        if (rawSecret != trimmedSecret) {
            log.warn("JWT secret contained whitespace. Automatically trimmed.")
        }
        if (trimmedSecret.length < 32) {
            throw IllegalStateException("JWT secret too short! Must be >= 32 characters. Current: ${trimmedSecret.length}")
        }
        key = Keys.hmacShaKeyFor(trimmedSecret.toByteArray(StandardCharsets.UTF_8))
        log.info("JWT Provider initialized. Secret length: {} chars", trimmedSecret.length)
    }

    fun generateAccessToken(user: User): String {
        val now = Date()
        val expiry = Date(now.time + jwtConfig.accessTokenExpiry)
        val roles = user.authorities.map(GrantedAuthority::getAuthority)

        return Jwts.builder()
            .subject(user.id)
            .claim("roles", roles)
            .claim("email", user.email)
            .claim("username", user.username)
            .claim("nicknameCompleted", user.nicknameCompleted)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun generateRefreshToken(user: User): String {
        val now = Date()
        val expiry = Date(now.time + jwtConfig.refreshTokenExpiry)
        val roles = user.authorities.map(GrantedAuthority::getAuthority)

        return Jwts.builder()
            .subject(user.id)
            .claim("email", user.email)
            .claim("username", user.username)
            .claim("roles", roles)
            .claim("nicknameCompleted", user.nicknameCompleted)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun getUserIdFromToken(token: String): String {
        return Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).payload.subject
    }

    fun getEmail(token: String): String {
        return Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).payload
            .get("email", String::class.java)
    }

    fun validateToken(token: String?): Boolean {
        if (token.isNullOrBlank()) return false
        return try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
            true
        } catch (e: MalformedJwtException) {
            log.error("Invalid JWT token: {}", e.message)
            false
        } catch (e: ExpiredJwtException) {
            false
        } catch (e: UnsupportedJwtException) {
            log.error("JWT token is unsupported: {}", e.message)
            false
        } catch (e: IllegalArgumentException) {
            log.error("JWT claims string is empty: {}", e.message)
            false
        }
    }

    fun getSubject(token: String): String {
        return Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).payload.subject
    }

    @Suppress("UNCHECKED_CAST")
    fun getRolesFromToken(token: String): List<String> {
        val claims = Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).payload
        return claims.get("roles", List::class.java) as List<String>
    }

    fun getKey(): SecretKey = key
}

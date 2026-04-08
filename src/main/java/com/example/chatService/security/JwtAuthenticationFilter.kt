package com.example.chatService.security

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    companion object {
        private val EXCLUDE_URLS = listOf(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/ws/token",
            "/login/oauth2/",
            "/oauth2/",
            "/login",
            "/error"
        )
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = resolveToken(request)
            if (token != null && jwtTokenProvider.validateToken(token)) {
                authenticateUser(token, request)
            }
        } catch (e: ExpiredJwtException) {
            request.setAttribute("expired", true)
        } catch (e: JwtException) {
            request.setAttribute("invalid", true)
        } catch (e: IllegalArgumentException) {
            request.setAttribute("invalid", true)
        }
        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7).trim()
        }
        return null
    }

    private fun authenticateUser(token: String, request: HttpServletRequest) {
        try {
            val roles = jwtTokenProvider.getRolesFromToken(token)
            val email = jwtTokenProvider.getEmail(token)
            val userDetails = userDetailsService.loadUserByUsername(email)

            val authentication = UsernamePasswordAuthenticationToken(
                userDetails, null,
                roles.map { SimpleGrantedAuthority(it) }
            )
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication
        } catch (e: Exception) {
            log.error("Cannot set user authentication: {}", e.message)
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return EXCLUDE_URLS.any { url -> path == url || path.startsWith("$url/") }
    }
}

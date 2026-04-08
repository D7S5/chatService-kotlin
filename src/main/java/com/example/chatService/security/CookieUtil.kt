package com.example.chatService.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service

@Service
class CookieUtil(
    private val jwtTokenProvider: JwtTokenProvider
) {
    companion object {
        private const val REFRESH_TOKEN_COOKIE = "refreshToken"
    }

    fun addRefreshTokenCookie(response: HttpServletResponse, refreshToken: String) {
        val cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/")
            .maxAge((60 * 60 * 24 * 7).toLong())
            .build()
        response.addHeader("Set-Cookie", cookie.toString())
    }

    fun clearRefreshTokenCookie(response: HttpServletResponse) {
        val cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(0)
            .sameSite("Lax")
            .build()
        response.addHeader("Set-Cookie", cookie.toString())
    }

    fun getRefreshToken(request: HttpServletRequest): String? {
        val cookies = request.cookies
        if (cookies == null) {
            println("No cookies in request")
            return null
        }
        return cookies.firstOrNull { it.name == REFRESH_TOKEN_COOKIE }?.value
    }

    fun tryResolveUserFromRefreshCookie(request: HttpServletRequest): String? {
        val refreshToken = getRefreshToken(request) ?: return null
        if (!jwtTokenProvider.validateToken(refreshToken)) return null
        return jwtTokenProvider.getSubject(refreshToken)
    }
}

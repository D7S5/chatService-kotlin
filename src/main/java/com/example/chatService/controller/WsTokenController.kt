package com.example.chatService.controller

import com.example.chatService.dto.WsTokenResponse
import com.example.chatService.redis.WsTokenService
import com.example.chatService.security.CookieUtil
import com.example.chatService.security.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ws")
class WsTokenController(
    private val wsTokenService: WsTokenService,
    private val cookieUtil: CookieUtil,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    @PostMapping("/token")
    fun createWsToken(req: HttpServletRequest): ResponseEntity<WsTokenResponse> {
        val refreshToken = cookieUtil.getRefreshToken(req)
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(WsTokenResponse(null, 0))
        }

        val userId = jwtTokenProvider.getSubject(refreshToken)
        val token = wsTokenService.createTokenForUser(userId)
        return ResponseEntity.ok(WsTokenResponse(token, 120))
    }
}

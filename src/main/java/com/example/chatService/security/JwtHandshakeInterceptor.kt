package com.example.chatService.security

import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class JwtHandshakeInterceptor(
    private val jwtProvider: JwtTokenProvider
) : HandshakeInterceptor {

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val uri = request.uri.toString()
        val token = if (uri.contains("token=")) {
            uri.substring(uri.indexOf("token=") + 6)
        } else null

        if (token == null || !jwtProvider.validateToken(token)) {
            println("WebSocket JWT 없음 또는 유효하지 않음")
            return false
        }

        val userId = jwtProvider.getSubject(token)
        attributes["userId"] = userId
        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {}
}

package com.example.chatService.component

import com.example.chatService.dto.UserEnterDto
import com.example.chatService.redis.OnlineStatusService
import com.example.chatService.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent

@Component
class WebSocketConnectHandler(
    private val onlineStatusService: OnlineStatusService,
    private val userRepository: UserRepository,
) : ApplicationListener<SessionConnectEvent> {
    override fun onApplicationEvent(event: SessionConnectEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val sessionAttrs = accessor.sessionAttributes
        val sessionId = accessor.sessionId

        if (sessionAttrs == null) {
            log.warn("Session attributes is null. sessionId={}", sessionId)
            return
        }

        val userId = sessionAttrs["userId"] as? String
        if (userId == null) {
            log.warn("WebSocket connect failed: missing userId. sessionId={}", sessionId)
            return
        }

        val user = userRepository.findById(userId).orElse(null)
        if (user == null) {
            log.warn("WebSocket connect failed: user not found. userId={}", userId)
            return
        }

        onlineStatusService.markOnline(
            UserEnterDto(userId, user.username),
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(WebSocketConnectHandler::class.java)
    }
}

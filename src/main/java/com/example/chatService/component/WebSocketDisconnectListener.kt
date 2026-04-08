package com.example.chatService.component

import com.example.chatService.redis.UserSessionRegistry
import com.example.chatService.service.RoomParticipantService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
class WebSocketDisconnectListener(
    private val roomParticipantService: RoomParticipantService,
    private val userSessionRegistry: UserSessionRegistry,
) {
    @EventListener
    fun handleDisconnect(event: SessionDisconnectEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val sessionId = accessor.sessionId ?: return
        val info = userSessionRegistry.remove(sessionId) ?: return

        try {
            roomParticipantService.leaveRoom(info.roomId, info.userId)
        } catch (e: Exception) {
            log.warn("disconnect leave failed roomId={}, userId={}", info.roomId, info.userId, e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(WebSocketDisconnectListener::class.java)
    }
}

package com.example.chatService.listener

import com.example.chatService.event.ParticipantForcedExitEvent
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ParticipantForcedExitListener(
    private val messagingTemplate: SimpMessagingTemplate,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: ParticipantForcedExitEvent) {
        messagingTemplate.convertAndSendToUser(
            event.userId,
            "/queue/room-force-exit",
            mapOf("roomId" to event.roomId, "reason" to event.reason),
        )
        log.info("[FORCED_EXIT] roomId={}, userId={}, reason={}", event.roomId, event.userId, event.reason)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ParticipantForcedExitListener::class.java)
    }
}

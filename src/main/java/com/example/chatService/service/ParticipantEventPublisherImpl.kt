package com.example.chatService.service

import com.example.chatService.dto.OwnerChangedEvent
import com.example.chatService.dto.ParticipantDto
import com.example.chatService.dto.ParticipantEvent
import com.example.chatService.dto.ParticipantEventType
import com.example.chatService.dto.RoomForceExitDto
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class ParticipantEventPublisherImpl(
    private val messagingTemplate: SimpMessagingTemplate,
) : ParticipantEventPublisher {
    override fun broadcastJoin(roomId: String, participant: ParticipantDto) {
        messagingTemplate.convertAndSend(
            "/topic/rooms/$roomId/participants",
            ParticipantEvent(ParticipantEventType.JOIN, roomId, participant, null),
        )
    }

    override fun broadcastLeave(roomId: String, participant: ParticipantDto) {
        messagingTemplate.convertAndSend(
            "/topic/rooms/$roomId/participants",
            ParticipantEvent(ParticipantEventType.LEAVE, roomId, participant, null),
        )
    }

    override fun broadcastLeave(roomId: String, participant: ParticipantDto, reason: String?) {
        messagingTemplate.convertAndSend(
            "/topic/rooms/$roomId/participants",
            ParticipantEvent(ParticipantEventType.LEAVE, roomId, participant, reason),
        )
        if (reason != null) {
            messagingTemplate.convertAndSendToUser(
                participant.userId,
                "/queue/room-force-exit",
                RoomForceExitDto(roomId, reason),
            )
        }
    }

    override fun broadcastOwnerChanged(roomId: String, newOwnerId: String) {
        messagingTemplate.convertAndSend(
            "/topic/room/$roomId/owner",
            OwnerChangedEvent(roomId, newOwnerId),
        )
        log.info("[OWNER_CHANGED] roomId={}, newOwnerId={}", roomId, newOwnerId)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ParticipantEventPublisherImpl::class.java)
    }
}

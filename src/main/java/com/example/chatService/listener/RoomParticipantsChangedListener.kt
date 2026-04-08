package com.example.chatService.listener

import com.example.chatService.dto.RoomCountDto
import com.example.chatService.event.RoomParticipantsChangedEvent
import com.example.chatService.repository.ChatRoomV2Repository
import com.example.chatService.service.RoomParticipantService
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class RoomParticipantsChangedListener(
    private val roomParticipantService: RoomParticipantService,
    private val messagingTemplate: SimpMessagingTemplate,
    private val roomRepository: ChatRoomV2Repository,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: RoomParticipantsChangedEvent) {
        val roomId = event.roomId
        val current = roomParticipantService.getCurrentCount(roomId)
        val max = roomRepository.findById(roomId).orElseThrow().maxParticipants

        messagingTemplate.convertAndSend("/topic/room-users/$roomId", "UPDATED")
        messagingTemplate.convertAndSend("/topic/rooms/$roomId/count", RoomCountDto(current, max))
    }
}

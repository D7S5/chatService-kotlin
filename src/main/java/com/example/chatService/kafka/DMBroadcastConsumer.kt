package com.example.chatService.kafka

import com.example.chatService.dto.DMMessageKafkaDto
import com.example.chatService.entity.DMMessage
import com.example.chatService.entity.DMRoom
import com.example.chatService.repository.DMRoomRepository
import com.example.chatService.service.DMService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

@Service
class DMBroadcastConsumer(
        private val messagingTemplate: SimpMessagingTemplate,
        private val roomRepository: DMRoomRepository,
        private val dmService: DMService
) {

    @KafkaListener(
            topics = ["dm-messages"],
            groupId = "\${chat.kafka.dm-consumer-group-id}",
            containerFactory = "DMKafkaListenerContainerFactory"
    )
    fun broadcast(dto: DMMessageKafkaDto) {

        val room: DMRoom = roomRepository.findById(dto.roomId)
                .orElseThrow { RuntimeException("Room not found") }

        val sentAt = OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(dto.sentAt),
                ZoneId.of("Asia/Seoul")
        )

        val message = DMMessage(
                room = room,
                senderId = dto.senderId,
                content = dto.content,
                sentAt = sentAt,
                isRead = false
        )

        val receiverId = dmService.getReceiverId(room.roomId!!, dto.senderId)

        messagingTemplate.convertAndSendToUser(receiverId, "/queue/dm", message)
        messagingTemplate.convertAndSendToUser(dto.senderId, "/queue/dm", message)
    }
}
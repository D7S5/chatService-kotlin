package com.example.chatService.kafka

import com.example.chatService.dto.DMMessageKafkaDto
import com.example.chatService.entity.DMMessage
import com.example.chatService.entity.DMRoom
import com.example.chatService.repository.DMMessageRepository
import com.example.chatService.repository.DMRoomRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

@Service
class DmStoreConsumer (
        private val roomRepository : DMRoomRepository,
        private val messageRepository : DMMessageRepository
){
    @KafkaListener(
            topics = ["dm-messages"],
            groupId = "dm-store",
            containerFactory = "DMKafkaListenerContainerFactory"
    )
    fun store(dto: DMMessageKafkaDto) {
        val room : DMRoom = roomRepository.findById(dto.roomId)
                .orElseThrow { RuntimeException("Room not found") }

        val sentAt = OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(dto.sentAt),
                ZoneId.of("Asia/Seoul")
        )

        val message = DMMessage(
                room = room,
                senderId = dto.senderId,
                senderName = dto.senderName,
                content = dto.content,
                sentAt = sentAt,
                isRead = false
        )
        messageRepository.save(message)
    }
}
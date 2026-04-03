package com.example.chatService.kafka

import com.example.chatService.dto.DMMessageKafkaDto
import com.example.chatService.dto.MessagingStatus
import com.example.chatService.entity.DMOutbox
import com.example.chatService.repository.DMOutboxRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class DMProducerService (
        private val outboxRepository : DMOutboxRepository
){
    @Transactional
    fun publish(dto : DMMessageKafkaDto) {
        val outbox = DMOutbox(
                roomId = dto.roomId,
                senderId = dto.senderId,
                senderName = dto.senderName,
                content = dto.content,
                eventTimestamp = dto.sentAt,
                status = MessagingStatus.NEW,
                createdAt = OffsetDateTime.now()
        )
        outboxRepository.save(outbox)
    }
}
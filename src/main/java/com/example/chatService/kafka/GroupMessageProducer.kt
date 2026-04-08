package com.example.chatService.kafka

import com.example.chatService.dto.GroupMessageDto
import com.example.chatService.dto.MessagingStatus
import com.example.chatService.entity.GroupOutbox
import com.example.chatService.repository.GroupMessageOutboxRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class GroupMessageProducer(
    private val outboxRepository: GroupMessageOutboxRepository,
) {
    fun send(dto: GroupMessageDto) {
        val message = GroupOutbox(
            roomId = dto.roomId,
            senderId = dto.senderId,
            senderName = dto.senderName,
            content = dto.content,
            eventTimestamp = dto.sentAt,
            status = MessagingStatus.NEW,
            createAt = OffsetDateTime.now(),
        )
        outboxRepository.save(message)
    }
}

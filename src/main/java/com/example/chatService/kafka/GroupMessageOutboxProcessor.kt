package com.example.chatService.kafka

import com.example.chatService.dto.GroupMessageDto
import com.example.chatService.dto.MessagingStatus
import com.example.chatService.repository.GroupMessageOutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupMessageOutboxProcessor(
    private val outboxRepository: GroupMessageOutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, GroupMessageDto>,
) {
    @Value("\${app.instance-id}")
    private lateinit var workerId: String

    @Transactional
    @Scheduled(fixedDelay = 50)
    fun processOutbox() {
        val claimed = outboxRepository.claimBatch(workerId, BATCH_SIZE)
        if (claimed == 0) {
            return
        }

        val list = outboxRepository.findByStatusAndLockedByOrderByIdAsc(MessagingStatus.PROCESSING, workerId)
        for (box in list) {
            try {
                val message = GroupMessageDto(
                    box.roomId ?: "",
                    box.senderId ?: "",
                    box.senderName ?: "",
                    box.content ?: "",
                    box.eventTimestamp,
                )
                kafkaTemplate.send(TOPIC, box.roomId ?: "", message)
                box.status = MessagingStatus.SENT
                box.lockedBy = null
                box.lockedAt = null
            } catch (e: Exception) {
                log.error("GroupOutbox processing failed for id={}", box.id, e)
                box.status = MessagingStatus.NEW
                box.lockedBy = null
                box.lockedAt = null
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(GroupMessageOutboxProcessor::class.java)
        private const val BATCH_SIZE = 100
        private const val TOPIC = "group-message-topic"
    }
}

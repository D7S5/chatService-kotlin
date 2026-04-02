package com.example.chatService.kafka

import com.example.chatService.dto.DMMessageKafkaDto
import com.example.chatService.dto.MessagingStatus
import com.example.chatService.entity.DMOutbox
import com.example.chatService.repository.DMOutboxRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DMOutboxProcessor(
        private val outboxRepository: DMOutboxRepository,
        private val kafkaTemplate: KafkaTemplate<String, DMMessageKafkaDto>
) {

    companion object {
        private const val TOPIC = "dm-messages"
        private const val BATCH_SIZE = 100
    }

    @Value("\${app.instance-id}")
    private lateinit var workerId: String

    @Transactional
    @Scheduled(fixedDelay = 50)
    @Throws(Exception::class)
    fun processOutbox() {
        val claimed = outboxRepository.claimBatch(workerId, BATCH_SIZE)
        if (claimed == 0) return

        val list: List<DMOutbox> =
                outboxRepository.findByStatusAndLockedByOrderByIdAsc(
                        MessagingStatus.PROCESSING,
                        workerId
                )

        for (box in list) {
            try {

                val roomId = box.roomId ?: throw IllegalStateException("roomId is null")
                val senderId = box.senderId ?: throw IllegalStateException("senderId is null")
                val content = box.content ?: throw IllegalStateException("content is null")

                val message = DMMessageKafkaDto(
                        roomId = roomId,
                        senderId = senderId,
                        content = content,
                        sentAt = box.eventTimestamp
                )

                kafkaTemplate.send(TOPIC, box.roomId, message).get()

                box.status = MessagingStatus.SENT
                box.lockedBy = null
                box.lockedAt = null

            } catch (e: Exception) {
                box.status = MessagingStatus.NEW
                box.lockedBy = null
                box.lockedAt = null
            }
        }
    }
}
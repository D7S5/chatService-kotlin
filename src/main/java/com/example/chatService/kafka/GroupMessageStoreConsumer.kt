package com.example.chatService.kafka

import com.example.chatService.dto.GroupMessageDto
import com.example.chatService.entity.GroupMessageEntity
import com.example.chatService.repository.GroupMessageRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class GroupMessageStoreConsumer(
    private val groupMessageRepository: GroupMessageRepository,
) {
    @KafkaListener(
        topics = [TOPIC],
        groupId = "group-chat-store",
        containerFactory = "groupKafkaListenerContainerFactory",
    )
    fun store(dto: GroupMessageDto) {
        val message = GroupMessageEntity.from(dto)
        groupMessageRepository.save(message)
    }

    companion object {
        private const val TOPIC = "group-message-topic"
    }
}

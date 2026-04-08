package com.example.chatService.kafka

import com.example.chatService.dto.GroupMessageDto
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class GroupMessageBroadcastConsumer(
    private val messagingTemplate: SimpMessagingTemplate,
) {
    @KafkaListener(
        topics = [TOPIC],
        groupId = "\${chat.kafka.consumer-group-id}",
        containerFactory = "groupKafkaListenerContainerFactory",
    )
    fun broadcast(dto: GroupMessageDto) {
        messagingTemplate.convertAndSend("/topic/chat/${dto.roomId}", dto)
    }

    companion object {
        private const val TOPIC = "group-message-topic"
    }
}

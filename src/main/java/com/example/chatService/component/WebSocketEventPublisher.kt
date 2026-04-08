package com.example.chatService.component

import com.example.chatService.dto.PublishAcceptFriendEvent
import com.example.chatService.dto.PublishFriendEvent
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class WebSocketEventPublisher(
    private val messagingTemplate: SimpMessagingTemplate,
) {
    fun publishFriendEvent(userId: String, publishFriendEvent: PublishFriendEvent) {
        messagingTemplate.convertAndSend("/topic/friends/$userId", publishFriendEvent)
    }

    fun publishAcceptFriendEvent(userId: String, publishFriendEvent: PublishAcceptFriendEvent) {
        messagingTemplate.convertAndSend("/topic/friends/$userId", publishFriendEvent)
    }
}

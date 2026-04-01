package com.example.chatService.service

import com.example.chatService.component.ChatRateLimiter
import com.example.chatService.dto.GroupMessageDto
import com.example.chatService.kafka.GroupMessageProducer
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class ChatMessageService(
        private val chatRateLimiter: ChatRateLimiter,
        private val messagingTemplate : SimpMessagingTemplate,
        private val groupMessageProducer: GroupMessageProducer
) {
    fun send(msg : GroupMessageDto, senderIdFromPrincipal : String) {
        msg.senderId = senderIdFromPrincipal

        if (!chatRateLimiter.allowUser(senderIdFromPrincipal)) {
            return
        }
        if (!chatRateLimiter.allowRoom(msg.roomId)) {
            return
        }

        // 밴이면 알림 보내고 차단
        if (!chatRateLimiter.allowOrBan(senderIdFromPrincipal)) {
            val ttl = chatRateLimiter.getBanTtl(senderIdFromPrincipal)
            messagingTemplate.convertAndSendToUser(
                    senderIdFromPrincipal,
                    "/queue/rate-limit",
                    mapOf(
                            "type" to "CHAT_BANNED",
                            "retryAfter" to ttl
                    )
            )
            return
        }
        groupMessageProducer.send(msg)
    }
}
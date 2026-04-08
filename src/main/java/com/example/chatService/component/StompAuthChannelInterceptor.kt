package com.example.chatService.component

import com.example.chatService.repository.RoomParticipantRepository
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessagingException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor

class StompAuthChannelInterceptor(
        private val repository : RoomParticipantRepository
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
                        ?: return message

        if (StompCommand.SUBSCRIBE == accessor.command) {
            val destination = accessor.destination ?: return message

            if (destination.startsWith("/topic/chat/")) {
                val roomId = destination.substring("/topic/chat/".length)
                val userId = accessor.user?.name
                        ?: throw MessagingException("UNAUTHORIZED")

                println("userId = $userId")

                val isParticipant =
                        repository.existsByRoomIdAndUserId(roomId, userId)
                if (!isParticipant) {
                    throw MessagingException("NOT_PARTICIPANTS");
                }

                val banned = repository.existsByRoomIdAndUserIdAndIsBannedTrue(roomId, userId)
                if (banned) {
                    throw MessagingException("BANNED")
                }
            }
        }

        return message
    }
}
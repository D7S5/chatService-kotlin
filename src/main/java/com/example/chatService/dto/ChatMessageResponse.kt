package com.example.chatService.dto

import com.example.chatService.entity.GroupMessageEntity

data class ChatMessageResponse(
        val roomId : String,
        val senderId : String,
        val senderName : String,
        val content : String,
        val sentAt : Long,
) {
    companion object {
        fun from(e : GroupMessageEntity) : ChatMessageResponse {
            return ChatMessageResponse(
                    roomId = e.roomId,
                    senderId = e.senderId,
                    senderName = e.senderName,
                    content = e.content,
                    sentAt = e.createdAt.toInstant().toEpochMilli()
            )
        }
    }
}
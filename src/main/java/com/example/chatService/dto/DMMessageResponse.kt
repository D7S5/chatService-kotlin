package com.example.chatService.dto

import java.time.OffsetDateTime

data class DmMessageResponse(
        val roomId: String,
        val senderId: String,
        val senderName: String,
        val content: String,
        val sentAt: OffsetDateTime,
        val isRead: Boolean
)
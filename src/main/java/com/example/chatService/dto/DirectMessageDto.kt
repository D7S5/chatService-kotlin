package com.example.chatService.dto

import java.time.LocalDateTime

data class DirectMessageDto(
    var roomId: String? = null,
    var senderId: String? = null,
    var content: String? = null,
    var timestamp: LocalDateTime? = null
)

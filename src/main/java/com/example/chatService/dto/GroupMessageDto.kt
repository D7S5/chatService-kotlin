package com.example.chatService.dto

data class GroupMessageDto(
        val roomId : String,
        var senderId : String? = null,
        val senderName : String,
        val content : String,
        val sentAt : Long
)

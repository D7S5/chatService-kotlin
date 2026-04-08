package com.example.chatService.dto

data class OwnerChangedEvent(
    val roomId: String,
    val newOwnerId: String
)

package com.example.chatService.dto

data class BanRequest(
    val targetUserId: String = "",
    val reason: String = ""
)

package com.example.chatService.dto

data class WsTokenResponse(
    val wsToken: String?,
    val expiresIn: Int
)

package com.example.chatService.dto

data class DMRoomDto(
    val roomId: String?,
    val targetUserId: String?,
    val targetUsername: String?,
    val unreadCount: Int
)

package com.example.chatService.dto

data class FriendRequestResponseDto(
        val fromUserId : String,
        val toUserId : String,
        val status : String
)

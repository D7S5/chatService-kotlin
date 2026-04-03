package com.example.chatService.dto

data class PublishFriendEvent(
        val type : FriendEventType,
        val fromUserId : String,
        val fromUserNickname: String?
)

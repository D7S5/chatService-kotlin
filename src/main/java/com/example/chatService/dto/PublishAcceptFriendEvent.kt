package com.example.chatService.dto

data class PublishAcceptFriendEvent(
        val type : FriendEventType,
        val friendId : String?
)

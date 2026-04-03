package com.example.chatService.dto

data class FriendRequestDto(
        var id: Long?,
        var fromUserId : String?,
        var fromUserNickname : String?,
        var toUserId : String?,
        var status : FriendStatus?
)

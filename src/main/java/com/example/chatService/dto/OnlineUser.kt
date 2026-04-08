package com.example.chatService.dto

data class OnlineUser(
    var userId: String = "",
    var username: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OnlineUser) return false
        return userId == other.userId
    }

    override fun hashCode(): Int = userId.hashCode()
}

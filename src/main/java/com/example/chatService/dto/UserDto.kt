package com.example.chatService.dto

import com.example.chatService.entity.Role
import com.example.chatService.entity.User

data class UserDto(
    val id: String?,
    val username: String?,
    val email: String?,
    val role: Role?,
    val online: Boolean = false,
    val nicknameCompleted: Boolean = false
) {
    companion object {
        fun from(user: User): UserDto {
            return UserDto(
                id = user.id,
                username = user.username,
                email = user.email,
                role = user.role,
                nicknameCompleted = user.nicknameCompleted
            )
        }
    }
}

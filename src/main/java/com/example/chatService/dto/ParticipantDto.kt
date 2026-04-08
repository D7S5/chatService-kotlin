package com.example.chatService.dto

import com.example.chatService.entity.Participant

data class ParticipantDto(
    val userId: String,
    val username: String?,
    val role: RoomRole
) {
    companion object {
        fun from(participant: Participant): ParticipantDto {
            return ParticipantDto(
                userId = participant.userId,
                username = null,
                role = participant.role!!
            )
        }
    }
}

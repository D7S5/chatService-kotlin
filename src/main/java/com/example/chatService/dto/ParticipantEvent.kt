package com.example.chatService.dto

data class ParticipantEvent(
    val type: ParticipantEventType,
    val roomId: String,
    val participant: ParticipantDto,
    val reason: String?
)

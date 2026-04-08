package com.example.chatService.event

data class ParticipantForcedExitEvent(
    val roomId: String,
    val userId: String,
    val reason: String
)

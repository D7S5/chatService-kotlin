package com.example.chatService.service

import com.example.chatService.dto.ParticipantDto

interface ParticipantEventPublisher {
    fun broadcastJoin(roomId: String, participant: ParticipantDto)

    fun broadcastLeave(roomId: String, participant: ParticipantDto)

    fun broadcastLeave(roomId: String, participant: ParticipantDto, reason: String?)

    fun broadcastOwnerChanged(roomId: String, newOwnerId: String)
}

package com.example.chatService.service

import com.example.chatService.dto.AdminChangedResponse
import com.example.chatService.dto.ParticipantDto
import com.example.chatService.dto.RoomRole
import com.example.chatService.entity.RoomParticipant

interface RoomParticipantService {
    fun joinRoom(roomId: String, userId: String)

    fun leaveRoom(roomId: String, userId: String)

    fun reconnect(roomId: String, userId: String)

    fun kick(roomId: String, targetUserId: String, byUserId: String)

    fun ban(roomId: String, targetUserId: String, byUserId: String, reason: String)

    fun toggleAdmin(roomId: String, requesterId: String, targetUserId: String): AdminChangedResponse

    fun changeRole(roomId: String, targetUserId: String, role: RoomRole, byUserId: String)

    fun transferOwnership(roomId: String, newOwnerId: String, byUserId: String)

    fun getCurrentCount(roomId: String): Int

    fun getActiveParticipants(roomId: String): List<RoomParticipant>

    fun getParticipants(roomId: String): List<ParticipantDto>

    fun broadcast(roomId: String)

    fun isParticipant(roomId: String, userId: String): Boolean
}

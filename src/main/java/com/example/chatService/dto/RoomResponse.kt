package com.example.chatService.dto

import com.example.chatService.entity.ChatRoom
import kotlin.math.max

data class RoomResponse(
        val roomId: String,
        val name : String,
        val type : RoomType?,
        val currentCount : Int,
        val maxParticipants : Int,

        val accessible : Boolean,
        val reason : String?,
        val largeRoom : Boolean,
        val ownerUserId : String,
        val inviteToken : String?
) {
    companion object {
        fun from(r : ChatRoom) : RoomResponse {
            return RoomResponse(
                    roomId = r.roomId,
                    name = r.name,
                    type = r.type,
                    currentCount = r.currentCount,
                    maxParticipants = r.maxParticipants,
                    accessible = true,
                    reason = null,
                    largeRoom = r.largeRoom,
                    ownerUserId = r.ownerUserId,
                    inviteToken = null
            )
        }
        fun of(r: ChatRoom, inviteToken: String?) : RoomResponse {
            return RoomResponse(
                    roomId = r.roomId,
                    name = r.name,
                    type = r.type,
                    currentCount = r.currentCount,
                    maxParticipants = r.maxParticipants,
                    accessible = true,
                    reason = null,
                    largeRoom = r.largeRoom,
                    ownerUserId = r.ownerUserId,
                    inviteToken = inviteToken
            )
        }
    }
}

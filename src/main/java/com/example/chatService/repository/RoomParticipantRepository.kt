package com.example.chatService.repository

import com.example.chatService.dto.RoomRole
import com.example.chatService.entity.RoomParticipant
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface RoomParticipantRepository : JpaRepository<RoomParticipant, Long> {
    fun findAllByRoomIdAndIsActiveTrue(roomId: String): List<RoomParticipant>

    fun existsByRoomIdAndUserIdAndRoleIn(roomId: String, userId: String, roles: List<RoomRole>): Boolean

    fun existsByRoomIdAndUserId(roomId: String, userId: String): Boolean

    fun findByRoomIdAndUserId(roomId: String, userId: String): Optional<RoomParticipant>

    fun existsByRoomIdAndUserIdAndRole(roomId: String, userId: String, role: RoomRole): Boolean

    fun existsByRoomIdAndUserIdAndIsBannedTrue(roomId: String, userId: String): Boolean

    fun countByRoomIdAndIsActiveTrue(roomId: String): Int
}

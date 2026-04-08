package com.example.chatService.repository

import com.example.chatService.entity.RoomBan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoomBanRepository : JpaRepository<RoomBan, Long> {
    fun existsByRoomIdAndUserId(roomId: String, userId: String): Boolean
}

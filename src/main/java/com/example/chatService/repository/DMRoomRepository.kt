package com.example.chatService.repository

import com.example.chatService.entity.DMRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface DMRoomRepository : JpaRepository<DMRoom, String> {
    fun findByUserAIdAndUserBId(a: String, b: String): Optional<DMRoom>

    fun findByUserBIdAndUserAId(a: String, b: String): Optional<DMRoom>

    @Query("SELECT r FROM DMRoom r WHERE r.userAId = :userId OR r.userBId = :userId ORDER BY r.lastMessageTime DESC")
    fun findByUser(@Param("userId") userId: String): List<DMRoom>
}

package com.example.chatService.repository

import com.example.chatService.entity.DMMessage
import com.example.chatService.entity.DMRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface DMMessageRepository : JpaRepository<DMMessage, Long> {
    fun findByRoomOrderBySentAtAsc(room: DMRoom): List<DMMessage>

    @Query("SELECT COUNT(m) FROM DMMessage m WHERE m.room.roomId = :roomId AND m.senderId <> :userId AND m.isRead = false")
    fun countUnread(@Param("roomId") roomId: String, @Param("userId") userId: String): Int

    @Modifying
    @Query("update DMMessage m set m.isRead = true where m.room.roomId = :roomId and m.senderId <> :userId and m.isRead = false")
    fun markAsReadByRoomAndReceiver(@Param("roomId") roomId: String, @Param("userId") userId: String)
}

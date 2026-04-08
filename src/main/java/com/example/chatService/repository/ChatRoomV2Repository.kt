package com.example.chatService.repository

import com.example.chatService.entity.ChatRoom
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ChatRoomV2Repository : JpaRepository<ChatRoom, String> {

    fun existsByRoomIdAndOwnerUserId(roomId: String, ownerId: String): Boolean

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ChatRoom r where r.roomId = :roomId")
    fun findByIdForUpdate(@Param("roomId") roomId: String): ChatRoom

    @Query("select r.roomId from ChatRoom r")
    fun findAllRoomIds(): List<String>
}

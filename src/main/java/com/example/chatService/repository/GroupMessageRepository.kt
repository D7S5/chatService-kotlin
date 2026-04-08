package com.example.chatService.repository

import com.example.chatService.entity.GroupMessageEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface GroupMessageRepository : JpaRepository<GroupMessageEntity, Long> {
    @Query(
        """
        SELECT m FROM GroupMessageEntity m
        WHERE m.roomId = :roomId
        ORDER BY m.createdAt DESC
    """,
    )
    fun findRecent(@Param("roomId") roomId: String, pageable: Pageable): List<GroupMessageEntity>
}

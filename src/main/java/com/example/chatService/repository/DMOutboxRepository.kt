package com.example.chatService.repository

import com.example.chatService.dto.MessagingStatus
import com.example.chatService.entity.DMOutbox
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface DMOutboxRepository : JpaRepository<DMOutbox, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
        UPDATE dm_outbox
        SET status = 'PROCESSING',
            locked_by = :workerId,
            locked_at = NOW()
        WHERE status = 'NEW'
        ORDER BY id
        LIMIT :limit
        """,
        nativeQuery = true,
    )
    fun claimBatch(@Param("workerId") workerId: String, @Param("limit") limit: Int): Int

    fun findByStatusAndLockedByOrderByIdAsc(status: MessagingStatus, lockedBy: String): List<DMOutbox>
}

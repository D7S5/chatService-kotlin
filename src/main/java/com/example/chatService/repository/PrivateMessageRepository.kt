package com.example.chatService.repository

import com.example.chatService.entity.PrivateMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PrivateMessageRepository : JpaRepository<PrivateMessage, Long> {
    fun findBySenderAndReceiverOrderBySentAtAsc(sender: String, receiver: String): List<PrivateMessage>

    fun findByReceiverAndSenderOrderBySentAtAsc(receiver: String, sender: String): List<PrivateMessage>
}

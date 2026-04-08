package com.example.chatService.repository

import com.example.chatService.entity.Message
import com.example.chatService.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface MessageRepository : JpaRepository<Message, String> {
    fun findBySenderAndReceiverOrderBySentAtAsc(sender: User, receiver: User): List<Message>

    fun findByReceiverAndIsReadFalse(receiver: User): List<Message>
}

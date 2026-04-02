package com.example.chatService.entity

import com.example.chatService.dto.RoomType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "chat_rooms")
class ChatRoom(

        @Id
        @Column(length = 36)
        var roomId: String = "",

        @Column(nullable = false)
        var name: String = "",

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        var type: RoomType? = null,

        @Column(nullable = false)
        var currentCount: Int = 0,

        @Column(nullable = false)
        var maxParticipants: Int = 0,

        @Version
        var version: Long = 0L,

        @Column(nullable = false)
        var largeRoom: Boolean = false,

        @Column(nullable = false)
        var ownerUserId: String = "",

        @Column(nullable = false)
        var createdAt: OffsetDateTime = OffsetDateTime.now()
) {

    fun increaseCount() {
        if (currentCount >= maxParticipants) {
            throw IllegalStateException("Room is full")
        }
        currentCount++
    }

    fun decreaseCount() {
        currentCount--
    }

    companion object {
        fun create(
                name: String,
                type: RoomType,
                maxParticipants: Int,
                ownerUserId: String
        ): ChatRoom {
            return ChatRoom().apply {
                roomId = UUID.randomUUID().toString()
                this.name = name
                this.type = type
                this.maxParticipants = maxParticipants
                largeRoom = maxParticipants >= 100
                this.ownerUserId = ownerUserId
                createdAt = OffsetDateTime.now()
            }
        }
    }
}
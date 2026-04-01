package com.example.chatService.entity

import com.example.chatService.dto.GroupMessageDto
import jakarta.persistence.*
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Entity
@Table(name = "group_messages")
class GroupMessageEntity (

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id : Long? = null,

        @Column(name = "room_Id" , nullable = false)
        var roomId: String = "",

        @Column(name = "sender_id", nullable = false)
        var senderId: String = "",

        @Column(name = "sender_name", nullable = false)
        var senderName: String = "",

        @Column(columnDefinition = "TEXT", nullable = false)
        var content : String = "",

        @Column(name = "created_at", nullable = false)
        var createdAt : OffsetDateTime = OffsetDateTime.now()
) {
    companion object {
        fun from(message : GroupMessageDto): GroupMessageEntity {
            return GroupMessageEntity(
                    roomId = message.roomId,
                    senderId = message.senderId ?: "",
                    senderName = message.senderName,
                    content = message.content,
                    createdAt = OffsetDateTime.ofInstant(
                            Instant.ofEpochMilli(message.sentAt),
                                    ZoneOffset.UTC)
                    )
        }
    }
}
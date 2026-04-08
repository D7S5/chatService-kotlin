package com.example.chatService.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "messages")
class Message(
    @Id
    @Column(length = 36, nullable = false)
    var id: String = UUID.randomUUID().toString(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    var sender: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    var receiver: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    var room: ChatRoom? = null,

    @Column(nullable = false, length = 2000)
    var content: String = "",

    @Column(nullable = false)
    var sentAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var isRead: Boolean = false
)

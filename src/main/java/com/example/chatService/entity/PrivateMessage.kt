package com.example.chatService.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "private_message")
class PrivateMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var sender: String = "",
    @Column(nullable = false)
    var receiver: String = "",
    @Column(nullable = false, length = 2000)
    var content: String = "",
    @Column(nullable = false)
    var sentAt: OffsetDateTime = OffsetDateTime.now(),
    var readFlag: Boolean = false
)

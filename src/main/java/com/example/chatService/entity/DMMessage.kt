package com.example.chatService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "dm_messages")
class DMMessage(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "room_id", referencedColumnName = "roomId")
        var room: DMRoom? = null,

        @Column(length = 36, nullable = false)
        var senderId: String? = null,

        @Column(nullable = false, columnDefinition = "TEXT")
        var content: String? = null,

        var sentAt: OffsetDateTime = OffsetDateTime.now(),

        @Column(name = "is_read")
        var isRead: Boolean = false
)
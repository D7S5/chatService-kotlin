package com.example.chatService.entity

import com.example.chatService.dto.MessagingStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "dm_outbox")
class DMOutbox(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        var roomId: String? = "",

        var senderId: String? = "",
        @Column(nullable = false, length = 40)
        var senderName: String? = "",

        @Column(columnDefinition = "TEXT")
        var content: String? = "",

        var eventTimestamp: Long = 0L,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        var status: MessagingStatus? = null,

        @Column(name = "locked_by", length = 64)
        var lockedBy: String? = null,

        @Column(name = "locked_at")
        var lockedAt: OffsetDateTime? = null,

        var createdAt: OffsetDateTime? = null
)
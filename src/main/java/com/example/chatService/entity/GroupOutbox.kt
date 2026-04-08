package com.example.chatService.entity

import com.example.chatService.dto.MessagingStatus
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "group_outbox")
class GroupOutbox(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var roomId: String? = null,
    var senderId: String? = null,
    var senderName: String? = null,
    @Column(columnDefinition = "TEXT")
    var content: String? = null,
    var eventTimestamp: Long = 0L,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: MessagingStatus? = null,
    @Column(name = "locked_by", length = 64)
    var lockedBy: String? = null,
    @Column(name = "locked_at")
    var lockedAt: OffsetDateTime? = null,
    var createAt: OffsetDateTime? = null
)

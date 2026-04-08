package com.example.chatService.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["roomId", "userId"])
    ]
)
class RoomBan(
    @Id
    @GeneratedValue
    var id: Long? = null,
    var roomId: String? = null,
    var userId: String? = null,
    var bannedBy: String? = null,
    var bannedAt: OffsetDateTime? = null
)

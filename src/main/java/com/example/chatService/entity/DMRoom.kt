package com.example.chatService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "dm_rooms")
class DMRoom (
        @Id
        @Column(length = 36)
        var roomId : String? = null,

        @Column(nullable = false, length = 36)
        var userAId : String? = null,
        @Column(nullable = false, length = 36)
        var userBId : String? = null,

        var lastMessageTime : OffsetDateTime = OffsetDateTime.now()
) {}
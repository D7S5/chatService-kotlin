package com.example.chatService.entity

import com.example.chatService.dto.RoomRole
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
        name = "room_participant",
        uniqueConstraints = [
            UniqueConstraint(
                    name = "uk_room_owner",
                    columnNames = ["owner_room_id"]
            )
        ]
)
class RoomParticipant(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @Column(name = "room_id", nullable = false, length = 36)
        var roomId: String = "",

        @Column(name = "user_id", nullable = false, length = 36)
        var userId : String = "",

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        var role : RoomRole? = null,

        @Column(nullable = false)
        var joinedAt: OffsetDateTime = OffsetDateTime.now(),

        @Column(name = "owner_room_id", unique = true)
        var ownerRoomId : String? = null,

        var lastActiveAt : OffsetDateTime? = null,

        @Column(nullable = false)
        var isActive: Boolean = false,

        var leftAt: OffsetDateTime? = null,

        @Column(nullable = false)
        var isBanned: Boolean = false,

        var bannedAt: OffsetDateTime? = null,

        @Column(length = 255)
        var banReason: String? = null,

        @Column(nullable = false)
        var createdAt: OffsetDateTime = OffsetDateTime.now(),

        @Column(nullable = false)
        var updatedAt: OffsetDateTime = OffsetDateTime.now()
) {

   @PrePersist
   fun onCreate() {
       val now = OffsetDateTime.now()
       joinedAt = now
       createdAt = now
       updatedAt = now
       isActive = false
       isBanned = false
   }

    @PreUpdate
    fun onUpdate() {
        updatedAt = OffsetDateTime.now()
    }

    fun activate() {
        isActive = true
        leftAt = null
        lastActiveAt = OffsetDateTime.now()
    }

    fun deactivate() {
        isActive = false
        leftAt = OffsetDateTime.now()
    }

    fun ban(reason : String) {
        isBanned = true
        isActive = false
        bannedAt = OffsetDateTime.now()
        banReason = reason
    }
    fun changeRole(role : RoomRole) {
        this.role = role
    }
}
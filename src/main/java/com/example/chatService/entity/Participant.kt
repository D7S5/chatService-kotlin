package com.example.chatService.entity

import com.example.chatService.dto.RoomRole
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.time.OffsetDateTime

@Entity
class Participant(
    @Id
    @GeneratedValue
    var id: Long? = null,
    var roomId: String? = null,
    var userId: String = "",
    var role: RoomRole? = null,
    var isActive: Boolean = false,
    var joinedAt: OffsetDateTime? = null,
    var leftAt: OffsetDateTime? = null
) {
    fun leave() {
        isActive = false
        leftAt = OffsetDateTime.now()
    }

    companion object {
        fun join(roomId: String, userId: String): Participant {
            return Participant().apply {
                this.roomId = roomId
                this.userId = userId
                this.isActive = true
                this.joinedAt = OffsetDateTime.now()
            }
        }
    }
}

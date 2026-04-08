package com.example.chatService.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "chat_message")
class ChatMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var roomId: String? = null,
    var sender: String? = null,
    var recipient: String? = null,
    var content: String? = null,
    @Enumerated(EnumType.STRING)
    var type: MessageType? = null,
    var timestamp: OffsetDateTime? = null
) {
    @PrePersist
    fun prePersist() {
        timestamp = OffsetDateTime.now()
    }

    constructor(content: String?, sender: String?, type: MessageType?) : this(
        content = content,
        sender = sender,
        type = type
    )

    enum class MessageType {
        CHAT, JOIN, LEAVE, DM
    }
}

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

    constructor(messageContent: String?, messageSender: String?, messageType: MessageType?) : this(
        content = messageContent,
        sender = messageSender,
        type = messageType
    )

    enum class MessageType {
        CHAT, JOIN, LEAVE, DM
    }
}

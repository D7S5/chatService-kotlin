package com.example.chatService.entity

import com.example.chatService.dto.FriendStatus
import jakarta.persistence.*

@Entity
class FriendRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id")
    var fromUser: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id")
    var toUser: User? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: FriendStatus? = null
)

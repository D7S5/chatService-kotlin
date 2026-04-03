package com.example.chatService.entity

import com.example.chatService.dto.FriendStatus
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*

@Entity
@Table(name = "friends")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
class Friend(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
        @JsonIgnoreProperties("hibernateLazyInitializer", "handler")
        var user: User? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "friend_id", referencedColumnName = "id", nullable = false)
        @JsonIgnoreProperties("hibernateLazyInitializer", "handler")
        var friend: User? = null,

        @ManyToOne
        var blockedBy: User? = null, // 누가 차단했는지

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        var status: FriendStatus? = null
)
package com.example.chatService.controller

import com.example.chatService.dto.FriendRequestSendDto
import com.example.chatService.security.UserPrincipal
import com.example.chatService.service.FriendService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user/friends")
class FriendRequestController(
    private val friendService: FriendService,
) {
    @PostMapping("/request")
    fun send(@RequestBody dto: FriendRequestSendDto): ResponseEntity<Any> =
        ResponseEntity.ok(
            friendService.sendFriendRequest(
                dto.fromUserId ?: throw IllegalArgumentException("fromUserId is required"),
                dto.toUserId ?: throw IllegalArgumentException("toUserId is required"),
            ),
        )

    @GetMapping("/received/{userId}")
    fun getReceived(@PathVariable userId: String): ResponseEntity<Any> =
        ResponseEntity.ok(friendService.getReceivedRequests(userId))

    @PostMapping("/accept/{id}")
    fun accept(@PathVariable id: Long): ResponseEntity<Any> =
        ResponseEntity.ok(friendService.acceptFriendRequest(id))

    @PostMapping("/reject/{id}")
    fun reject(
        @PathVariable id: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
    ): ResponseEntity<Any> = ResponseEntity.ok(friendService.reject(id, userPrincipal))

    @GetMapping("/list/{userId}")
    fun friendList(@PathVariable userId: String): ResponseEntity<Any> =
        ResponseEntity.ok(friendService.getFriendList(userId))

    @DeleteMapping("/{friendUserId}")
    fun removeFriend(
        @PathVariable friendUserId: String,
        authentication: Authentication,
    ): ResponseEntity<Void> {
        val principal = authentication.principal as UserPrincipal
        friendService.removeFriend(principal.id, friendUserId)
        return ResponseEntity.ok().build()
    }
}

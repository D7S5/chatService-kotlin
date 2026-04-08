package com.example.chatService.controller

import com.example.chatService.dto.CreateRoomRequest
import com.example.chatService.dto.GroupMessageDto
import com.example.chatService.dto.InviteEnterResponse
import com.example.chatService.dto.RoomResponse
import com.example.chatService.entity.ChatRoom
import com.example.chatService.security.UserPrincipal
import com.example.chatService.service.ChatMessageService
import com.example.chatService.service.ChatRoomService
import com.example.chatService.service.RoomInviteService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/api/rooms")
class ChatController(
    private val chatMessageService: ChatMessageService,
    private val chatRoomV2Service: ChatRoomService,
    private val inviteService: RoomInviteService,
) {
    @MessageMapping("/chat.send")
    fun send(msg: GroupMessageDto, principal: Principal) {
        chatMessageService.send(msg, principal.name)
    }

    @PostMapping("/create")
    fun create(
        @RequestBody request: CreateRoomRequest,
        @AuthenticationPrincipal user: UserPrincipal,
    ): RoomResponse = chatRoomV2Service.create(request, user.id)

    @GetMapping("/invite/{token}")
    fun enterByInvite(@PathVariable token: String): ResponseEntity<InviteEnterResponse> =
        ResponseEntity.ok(InviteEnterResponse(inviteService.enterByInvite(token)))

    @GetMapping
    fun getChatRooms(): ResponseEntity<List<ChatRoom>> =
        try {
            ResponseEntity.ok(chatRoomV2Service.getAllRooms())
        } catch (_: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
}

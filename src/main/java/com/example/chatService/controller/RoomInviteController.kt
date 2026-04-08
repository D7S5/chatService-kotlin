package com.example.chatService.controller

import com.example.chatService.dto.InviteCodeResponse
import com.example.chatService.dto.InviteJoinRequest
import com.example.chatService.dto.JoinByInviteResponse
import com.example.chatService.dto.RoomResponse
import com.example.chatService.security.UserPrincipal
import com.example.chatService.service.ChatRoomService
import com.example.chatService.service.RoomInviteService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/rooms")
class RoomInviteController(
    private val inviteService: RoomInviteService,
    private val service: ChatRoomService,
) {
    @PostMapping("/join-by-invite")
    fun joinByInvite(
        @RequestBody req: InviteJoinRequest,
        @AuthenticationPrincipal user: UserPrincipal,
    ): JoinByInviteResponse = inviteService.joinByInvite(req.inviteCode, user.id)

    @PostMapping("/{roomId}/invite/reissue")
    fun reissueInvite(
        @PathVariable roomId: String,
        @AuthenticationPrincipal user: UserPrincipal,
    ): InviteCodeResponse = InviteCodeResponse(inviteService.generateInviteCode(roomId, user.id))

    @GetMapping("/{roomId}")
    fun getRoom(
        @PathVariable roomId: String,
        @AuthenticationPrincipal user: UserPrincipal,
    ): RoomResponse = service.getRoom(roomId, user.id)
}

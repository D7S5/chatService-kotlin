package com.example.chatService.controller

import com.example.chatService.dto.AdminGrantRequest
import com.example.chatService.dto.BanRequest
import com.example.chatService.dto.KickRequest
import com.example.chatService.security.UserPrincipal
import com.example.chatService.service.RoomParticipantService
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/api/rooms/{roomId}")
class RoomAdminController(
    private val service: RoomParticipantService,
    private val messagingTemplate: SimpMessagingTemplate,
) {
    @PostMapping("/kick")
    fun kickUser(
        @PathVariable roomId: String,
        @RequestBody request: KickRequest,
        @AuthenticationPrincipal me: UserPrincipal,
    ): ResponseEntity<Void> {
        service.kick(roomId, request.targetUserId, me.id)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/ban")
    fun banUser(
        @PathVariable roomId: String,
        @RequestBody request: BanRequest,
        @AuthenticationPrincipal me: UserPrincipal,
    ): ResponseEntity<Void> {
        service.ban(roomId, request.targetUserId, me.id, request.reason)
        return ResponseEntity.ok().build()
    }

    @MessageMapping("/rooms/{roomId}/admin")
    fun grantAdmin(
        @DestinationVariable roomId: String,
        @Payload request: AdminGrantRequest,
        principal: Principal,
    ) {
        val result = service.toggleAdmin(roomId, principal.name, request.targetUserId)
        messagingTemplate.convertAndSend("/topic/rooms/$roomId/participants", result)
    }
}

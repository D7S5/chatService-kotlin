package com.example.chatService.controller

import com.example.chatService.dto.CurrentCountResponse
import com.example.chatService.dto.ParticipantDto
import com.example.chatService.security.UserPrincipal
import com.example.chatService.service.ChatRoomService
import com.example.chatService.service.RoomParticipantService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/rooms/")
class RoomParticipantController(
    private val service: RoomParticipantService,
    private val chatRoomService: ChatRoomService,
) {
    @PostMapping("/{roomId}/participants")
    fun join(
        @PathVariable roomId: String,
        @AuthenticationPrincipal user: UserPrincipal,
    ): ResponseEntity<Void> {
        chatRoomService.joinRoom(roomId, user.id)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{roomId}/participants")
    fun leave(
        @PathVariable roomId: String,
        @AuthenticationPrincipal user: UserPrincipal,
    ): ResponseEntity<Void> {
        service.leaveRoom(roomId, user.id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{roomId}/participants")
    fun getParticipants(@PathVariable roomId: String): List<ParticipantDto> = service.getParticipants(roomId)

    @GetMapping("/{roomId}/count")
    fun getRoomCount(@PathVariable roomId: String): CurrentCountResponse =
        CurrentCountResponse(service.getCurrentCount(roomId))
}

package com.example.chatService.controller

import com.example.chatService.dto.ChatMessageResponse
import com.example.chatService.dto.RoomResponse
import com.example.chatService.service.GroupRoomService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/rooms")
class GroupRoomController(
    private val groupRoomService: GroupRoomService,
) {
    @GetMapping("/{roomId}/messages")
    fun messages(@PathVariable roomId: String, @RequestParam(defaultValue = "50") limit: Int): List<ChatMessageResponse> =
        groupRoomService.messages(roomId, limit)

    @GetMapping("/with-count")
    fun getRoomsWithCount(): List<RoomResponse> = groupRoomService.getRoomsWithCount()
}

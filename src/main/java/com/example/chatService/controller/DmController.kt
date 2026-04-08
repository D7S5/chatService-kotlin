package com.example.chatService.controller

import com.example.chatService.dto.DmStartDto
import com.example.chatService.entity.DMMessage
import com.example.chatService.service.DMService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dm")
class DmController(
    private val dmService: DMService,
) {
    @PostMapping("/start")
    fun startDM(@RequestBody dto: DmStartDto): ResponseEntity<Any> =
        ResponseEntity.ok(dmService.startOrGetRoom(dto.userA, dto.userB))

    @GetMapping("/messages/{roomId}")
    fun getMessages(@PathVariable roomId: String): List<DMMessage> = dmService.getMessages(roomId)

    @GetMapping("/list/{userId}")
    fun getUserDMs(@PathVariable userId: String): ResponseEntity<Any> =
        ResponseEntity.ok(dmService.getUserRoomsWithUnread(userId))

    @PutMapping("/messages/{roomId}/read")
    fun markRead(@PathVariable roomId: String, @RequestParam userId: String): ResponseEntity<Void> {
        dmService.markMessagesAsRead(roomId, userId)
        return ResponseEntity.ok().build()
    }
}

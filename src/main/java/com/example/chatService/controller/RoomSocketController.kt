package com.example.chatService.controller

import com.example.chatService.dto.UserRoomSession
import com.example.chatService.redis.UserSessionRegistry
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RoomSocketController(
    private val userSessionRegistry: UserSessionRegistry,
) {
    @MessageMapping("/room.enter")
    fun enterRoom(request: UserRoomSession, @Header("simpSessionId") sessionId: String) {
        userSessionRegistry.save(sessionId, request.userId, request.roomId)
    }
}

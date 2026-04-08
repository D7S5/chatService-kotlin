package com.example.chatService.controller

import com.example.chatService.dto.UserEnterDto
import com.example.chatService.redis.OnlineStatusService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.web.bind.annotation.RestController

@RestController
class RedisController(
    private val onlineStatusService: OnlineStatusService,
) {
    @MessageMapping("/user.enter")
    fun userEnter(dto: UserEnterDto) {
        if (dto.userId == null || dto.username == null) {
            return
        }
        onlineStatusService.markOnline(dto)
    }

    @MessageMapping("/user.heartbeat")
    fun heartbeat(accessor: SimpMessageHeaderAccessor) {
        val userId = accessor.sessionAttributes?.get("userId") as? String ?: return
        onlineStatusService.refreshTTL(userId)
    }

    @MessageMapping("/user.leave")
    fun leave(accessor: SimpMessageHeaderAccessor) {
        val userId = accessor.sessionAttributes?.get("userId") as? String ?: return
        onlineStatusService.markOffline(userId)
    }
}

package com.example.chatService.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.FORBIDDEN)
class BannedFromRoomException(roomId: String) : RuntimeException("You are banned from room: $roomId")

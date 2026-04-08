package com.example.chatService.controller

import com.example.chatService.dto.NicknameDto
import com.example.chatService.dto.NicknameRequest
import com.example.chatService.security.UserPrincipal
import com.example.chatService.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService,
) {
    @PostMapping("/set-nickname")
    fun setNickname(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: NicknameRequest,
    ): ResponseEntity<NicknameDto> {
        val trimmed = userService.setNickname(principal.id, request.nickname)
        return ResponseEntity.ok(NicknameDto(trimmed))
    }
}

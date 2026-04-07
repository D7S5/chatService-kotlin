package com.example.chatService.oauth

import com.example.chatService.dto.OAuthNicknameRequest
import com.example.chatService.dto.UserDto
import com.example.chatService.repository.UserRepository
import com.example.chatService.security.UserPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class OAuthApi (
        private val userRepository: UserRepository
){
    @PostMapping("/user/oauth/nickname")
    fun setNickname(@RequestBody request : OAuthNicknameRequest,
                    authentication : Authentication) : ResponseEntity<UserDto> {
        val nickname = request.nickname

        val principal = authentication.principal as UserPrincipal
        val user = userRepository.findById(principal.id).orElseThrow()

        user.usernameValue = nickname
        user.nicknameCompleted = true

        userRepository.save(user)

        return ResponseEntity.ok(UserDto.from(user))
    }

    @GetMapping("/me")
    fun me(authentication: Authentication) : ResponseEntity<UserDto> {
        val principal = authentication.principal as UserPrincipal

        val user = userRepository.findById(principal.id).orElseThrow()

        return ResponseEntity.ok(UserDto.from(user))
    }
}
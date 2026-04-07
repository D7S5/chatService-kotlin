package com.example.chatService.controller

import com.example.chatService.dto.JwtResponse
import com.example.chatService.dto.LoginRequest
import com.example.chatService.dto.LoginResponse
import com.example.chatService.dto.RegisterRequest
import com.example.chatService.security.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController (
        private val authService: AuthService
){

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest,
              response : HttpServletResponse) : ResponseEntity<LoginResponse> {
        val res = authService.login(request, response)
        return ResponseEntity.ok(res)
    }

    @PostMapping("/refresh")
    fun refreshToken(response : HttpServletResponse,
                     request : HttpServletRequest)
    : ResponseEntity<JwtResponse> {
        val res = authService.refresh(request, response)
        return ResponseEntity.ok(res)
    }

    @PostMapping("/logout")
    fun logout(
            response: HttpServletResponse,
            authentication : Authentication?,
            request: HttpServletRequest
    ): ResponseEntity<Void> {
        authService.logout(response, authentication, request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/register")
    fun register(
            @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<String> {
        authService.register(request)
        return ResponseEntity.ok("User registered successfully")
    }
}
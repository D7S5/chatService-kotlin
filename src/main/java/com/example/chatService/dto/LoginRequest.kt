package com.example.chatService.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "유효한 이메일 형식이어야 합니다.")
    val email: String,
    @field:NotBlank(message = "비밀번호는 비어있을 수 없습니다.")
    val password: String
)

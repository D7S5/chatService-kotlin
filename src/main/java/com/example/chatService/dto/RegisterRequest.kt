package com.example.chatService.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class RegisterRequest(
    @field:NotBlank(message = "아이디는 필수입니다")
    val username: String,
    @field:NotBlank(message = "비밀번호는 필수입니다")
    val password: String,
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "유효한 이메일 형식이 아닙니다")
    val email: String
) {
    override fun toString(): String {
        return "RegisterRequest[username=$username, password=****, email=$email]"
    }
}

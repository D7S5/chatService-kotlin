package com.example.chatService.service

import com.example.chatService.repository.UserRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
        private val userRepository: UserRepository,
        private val redisTemplate: StringRedisTemplate,
) {
    @Transactional
    fun setNickname(userId: String, username: String) : String {
        val trimmed = username.trim();

        if (trimmed.isEmpty()) {
            throw IllegalArgumentException("닉네임은 비어있을 수 없습니다.")
        }

        val user = userRepository.findById(userId)
                .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        if (userRepository.existsByUsernameValue(trimmed)) {
            throw IllegalArgumentException("이미 사용중인 닉네임 입니다.")
        }

        try {
            user.usernameValue = trimmed
            user.nicknameCompleted = true
            userRepository.save(user)
        } catch (e : DataIntegrityViolationException) {
            throw IllegalArgumentException("이미 사용중인 닉네임 입니다.")
        }

        val key = "user:$userId:username"
        redisTemplate.delete(key)

        return trimmed
    }

}
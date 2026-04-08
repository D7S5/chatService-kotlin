package com.example.chatService.redis

import com.example.chatService.dto.UserRoomSession
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class UserSessionRegistry(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) {
    fun save(sessionId: String, userId: String, roomId: String) {
        val value = UserRoomSession(userId, roomId)
        try {
            val json = objectMapper.writeValueAsString(value)
            redisTemplate.opsForValue().set(key(sessionId), json, TTL)
        } catch (e: JsonProcessingException) {
            throw IllegalStateException("웹소켓 세션 저장 실패", e)
        }
    }

    fun get(sessionId: String): UserRoomSession? {
        val json = redisTemplate.opsForValue().get(key(sessionId)) ?: return null
        try {
            return objectMapper.readValue(json, UserRoomSession::class.java)
        } catch (e: JsonProcessingException) {
            throw IllegalStateException("웹소켓 세션 조회 실패", e)
        }
    }

    fun remove(sessionId: String): UserRoomSession? {
        val redisKey = key(sessionId)
        val json = redisTemplate.opsForValue().get(redisKey) ?: return null
        redisTemplate.delete(redisKey)

        try {
            return objectMapper.readValue(json, UserRoomSession::class.java)
        } catch (e: JsonProcessingException) {
            throw IllegalStateException("웹소켓 세션 삭제 실패", e)
        }
    }

    private fun key(sessionId: String): String = "$PREFIX$sessionId"

    companion object {
        private const val PREFIX = "ws:session:"
        private val TTL: Duration = Duration.ofHours(6)
    }
}

package com.example.chatService.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

@Service
class WsTokenService(
    private val redisTemplate: RedisTemplate<String, Any>,
) {
    fun createTokenForUser(userId: String): String {
        val token = UUID.randomUUID().toString()
        val key = "$KEY_PREFIX$token"
        redisTemplate.opsForValue().set(key, userId, Duration.ofSeconds(TTL_SECONDS.toLong()))
        return token
    }

    fun consumeToken(token: String): String? {
        val key = "$KEY_PREFIX$token"
        val userId = redisTemplate.opsForValue().get(key) as? String
        if (userId != null) {
            redisTemplate.delete(key)
        }
        return userId
    }

    fun peekToken(token: String): String? = redisTemplate.opsForValue().get("$KEY_PREFIX$token") as? String

    companion object {
        private const val KEY_PREFIX = "ws_token:"
        private const val TTL_SECONDS = 120
    }
}

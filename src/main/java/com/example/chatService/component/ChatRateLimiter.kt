package com.example.chatService.component

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class ChatRateLimiter(
    private val redis: StringRedisTemplate,
) {
    fun getBanTtl(userId: String): Long = redis.getExpire(banKey(userId)) ?: 0

    fun allowOrBan(userId: String): Boolean {
        if (isBanned(userId)) {
            return false
        }

        val count = redis.opsForValue().increment(rateKey(userId))
        if (count == 1L) {
            redis.expire(rateKey(userId), Duration.ofSeconds(1))
        }

        if (count != null && count > USER_LIMIT_PER_SEC) {
            redis.opsForValue().set(
                banKey(userId),
                "1",
                Duration.ofSeconds(BAN_SECONDS.toLong()),
            )
            return false
        }

        return true
    }

    fun allowUser(userId: String): Boolean {
        val key = "chat:rate:user:$userId"
        val count = redis.opsForValue().increment(key)
        if (count == 1L) {
            redis.expire(key, Duration.ofSeconds(1))
        }
        return count != null && count <= USER_LIMIT_PER_SEC
    }

    fun allowRoom(roomId: String): Boolean {
        val key = "chat:rate:room:$roomId"
        val count = redis.opsForValue().increment(key)
        if (count == 1L) {
            redis.expire(key, Duration.ofSeconds(1))
        }
        return count != null && count <= ROOM_LIMIT_PER_SEC
    }

    private fun rateKey(userId: String): String = "chat:rate:user:$userId"

    private fun banKey(userId: String): String = "chat:ban:user$userId"

    private fun isBanned(userId: String): Boolean = redis.hasKey(banKey(userId)) == true

    companion object {
        private const val USER_LIMIT_PER_SEC = 7
        private const val ROOM_LIMIT_PER_SEC = 1000
        private const val BAN_SECONDS = 30
    }
}

package com.example.chatService.redis

import com.example.chatService.dto.OnlineStatusDto
import com.example.chatService.dto.UserEnterDto
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class OnlineStatusService(
    private val redisTemplate: StringRedisTemplate,
    private val messagingTemplate: SimpMessagingTemplate,
) {
    fun markOnline(dto: UserEnterDto) {
        val userKey = dto.userId ?: return
        val username = dto.username ?: return
        redisTemplate.opsForHash<String, String>().put(ONLINE_HASH, userKey, username)
        redisTemplate.opsForValue().set("$TTL_KEY_PREFIX$userKey", "1", EXPIRE_MINUTES)
        broadcastOnlineUsers()
    }

    fun refreshTTL(userId: String) {
        redisTemplate.expire("$TTL_KEY_PREFIX$userId", EXPIRE_MINUTES)
    }

    fun markOffline(userId: String) {
        redisTemplate.opsForHash<String, String>().delete(ONLINE_HASH, userId)
        redisTemplate.delete("$TTL_KEY_PREFIX$userId")
        broadcastOnlineUsers()
    }

    fun getAllOnlineUsers(): Set<OnlineStatusDto> {
        val entries = redisTemplate.opsForHash<String, String>().entries(ONLINE_HASH)
        return entries.entries.mapTo(linkedSetOf()) { OnlineStatusDto(it.key, it.value, true) }
    }

    fun broadcastOnlineUsers() {
        messagingTemplate.convertAndSend("/topic/online-users", getAllOnlineUsers())
    }

    @Scheduled(fixedRate = 5000)
    fun cleanExpiredUsersV2() {
        val allUsers = redisTemplate.opsForHash<String, String>().keys(ONLINE_HASH)
        var changed = false

        for (userId in allUsers) {
            if (redisTemplate.hasKey("$TTL_KEY_PREFIX$userId") != true) {
                redisTemplate.opsForHash<String, String>().delete(ONLINE_HASH, userId)
                changed = true
            }
        }

        if (changed) {
            broadcastOnlineUsers()
        }
    }

    companion object {
        private const val ONLINE_HASH = "online:users"
        private const val TTL_KEY_PREFIX = "online:ttl:"
        private val EXPIRE_MINUTES: Duration = Duration.ofMinutes(2)
    }
}

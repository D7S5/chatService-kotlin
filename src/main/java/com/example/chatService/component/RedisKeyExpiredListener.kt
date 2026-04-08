package com.example.chatService.component

import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Component

@Component
class RedisKeyExpiredListener : MessageListener {
    override fun onMessage(message: Message, pattern: ByteArray?) {
        val expiredKey = String(message.body)
        if (!expiredKey.startsWith(TTL_KEY_PREFIX)) {
            return
        }

        val userId = expiredKey.removePrefix(TTL_KEY_PREFIX)
        log.info("User offline => {}", userId)
    }

    companion object {
        private val log = LoggerFactory.getLogger(RedisKeyExpiredListener::class.java)
        private const val TTL_KEY_PREFIX = "online:ttl:"
    }
}

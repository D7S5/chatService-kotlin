package com.example.chatService.component

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration

@ExtendWith(MockitoExtension::class)
class ChatRateLimiterTest {
    private val redis: StringRedisTemplate = mock()
    private val valueOperations: ValueOperations<String, String> = mock()

    private val limiter = ChatRateLimiter(redis)

    @Test
    fun `allowOrBan returns false for banned user`() {
        whenever(redis.hasKey("chat:ban:useruser-1")).thenReturn(true)

        val allowed = limiter.allowOrBan("user-1")

        assertFalse(allowed)
        verify(redis, never()).opsForValue()
    }

    @Test
    fun `allowOrBan allows first request and sets one second expiry`() {
        whenever(redis.hasKey("chat:ban:useruser-1")).thenReturn(false)
        whenever(redis.opsForValue()).thenReturn(valueOperations)
        whenever(valueOperations.increment("chat:rate:user:user-1")).thenReturn(1L)

        val allowed = limiter.allowOrBan("user-1")

        assertTrue(allowed)
        verify(redis).expire("chat:rate:user:user-1", Duration.ofSeconds(1))
        verify(valueOperations, never()).set(eq("chat:ban:useruser-1"), any<String>(), any<Duration>())
    }

    @Test
    fun `allowOrBan bans user after limit exceeded`() {
        whenever(redis.hasKey("chat:ban:useruser-1")).thenReturn(false)
        whenever(redis.opsForValue()).thenReturn(valueOperations)
        whenever(valueOperations.increment("chat:rate:user:user-1")).thenReturn(8L)

        val allowed = limiter.allowOrBan("user-1")

        assertFalse(allowed)
        verify(valueOperations, times(1)).set("chat:ban:useruser-1", "1", Duration.ofSeconds(30))
    }
}

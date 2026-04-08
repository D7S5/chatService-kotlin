package com.example.chatService.redis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration

@ExtendWith(MockitoExtension::class)
class WsTokenServiceTest {
    private val redisTemplate: RedisTemplate<String, Any> = mock()
    private val valueOperations: ValueOperations<String, Any> = mock()

    private val service = WsTokenService(redisTemplate)

    @Test
    fun `createTokenForUser stores token with ttl`() {
        val keyCaptor = argumentCaptor<String>()
        val valueCaptor = argumentCaptor<Any>()
        val ttlCaptor = argumentCaptor<Duration>()
        whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)

        val token = service.createTokenForUser("user-1")

        assertNotNull(token)
        verify(valueOperations, times(1)).set(keyCaptor.capture(), valueCaptor.capture(), ttlCaptor.capture())
        assertEquals("ws_token:$token", keyCaptor.firstValue)
        assertEquals("user-1", valueCaptor.firstValue)
        assertEquals(Duration.ofSeconds(120), ttlCaptor.firstValue)
    }

    @Test
    fun `consumeToken returns user id and deletes token`() {
        whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
        whenever(valueOperations.get("ws_token:token-1")).thenReturn("user-1")

        val consumed = service.consumeToken("token-1")

        assertEquals("user-1", consumed)
        verify(redisTemplate).delete("ws_token:token-1")
    }

    @Test
    fun `consumeToken returns null when token does not exist`() {
        whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
        whenever(valueOperations.get("ws_token:missing")).thenReturn(null)

        val consumed = service.consumeToken("missing")

        assertNull(consumed)
        verify(redisTemplate, never()).delete("ws_token:missing")
    }
}

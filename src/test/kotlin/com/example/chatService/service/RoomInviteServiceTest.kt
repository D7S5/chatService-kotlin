package com.example.chatService.service

import com.example.chatService.dto.RoomRole
import com.example.chatService.dto.RoomType
import com.example.chatService.entity.ChatRoom
import com.example.chatService.repository.ChatRoomV2Repository
import com.example.chatService.repository.RoomParticipantRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class RoomInviteServiceTest {
    private val redis: StringRedisTemplate = mock()
    private val valueOperations: ValueOperations<String, String> = mock()
    private val roomRepository: ChatRoomV2Repository = mock()
    private val roomParticipantService: RoomParticipantService = mock()
    private val participantRepository: RoomParticipantRepository = mock()

    private val service = RoomInviteService(
        redis,
        roomRepository,
        roomParticipantService,
        participantRepository,
    )

    @Test
    fun `joinByInvite joins private room and returns room id`() {
        val room = ChatRoom.create("secret", RoomType.PRIVATE, 10, "owner-1")

        whenever(redis.opsForValue()).thenReturn(valueOperations)
        whenever(valueOperations.get("room:invite:invite-123")).thenReturn(room.roomId)
        whenever(roomRepository.findById(room.roomId)).thenReturn(Optional.of(room))

        val response = service.joinByInvite("invite-123", "user-1")

        assertEquals(room.roomId, response.roomId)
        verify(roomParticipantService).joinRoom(room.roomId, "user-1")
    }

    @Test
    fun `generateInviteCode rejects non admin requester`() {
        whenever(participantRepository.existsByRoomIdAndUserIdAndRoleIn(any(), any(), any())).thenReturn(false)

        assertThrows(AccessDeniedException::class.java) {
            service.generateInviteCode("room-1", "user-1")
        }

        verify(roomRepository, never()).findById(any())
    }

    @Test
    fun `generateInviteCode stores invite in redis with ttl`() {
        val room = ChatRoom.create("secret", RoomType.PRIVATE, 10, "owner-1")
        val keyCaptor = argumentCaptor<String>()
        val roomIdCaptor = argumentCaptor<String>()
        val ttlCaptor = argumentCaptor<Duration>()

        whenever(redis.opsForValue()).thenReturn(valueOperations)
        whenever(participantRepository.existsByRoomIdAndUserIdAndRoleIn(eq(room.roomId), eq("admin-1"), any())).thenReturn(true)
        whenever(roomRepository.findById(room.roomId)).thenReturn(Optional.of(room))

        val inviteCode = service.generateInviteCode(room.roomId, "admin-1")

        assertEquals(36, inviteCode.length)
        verify(valueOperations, times(1)).set(keyCaptor.capture(), roomIdCaptor.capture(), ttlCaptor.capture())
        assertEquals("room:invite:$inviteCode", keyCaptor.firstValue)
        assertEquals(room.roomId, roomIdCaptor.firstValue)
        assertEquals(Duration.ofMinutes(10), ttlCaptor.firstValue)
    }

    @Test
    fun `enterByInvite throws gone when invite token expired`() {
        whenever(redis.opsForValue()).thenReturn(valueOperations)
        whenever(valueOperations.get("room:invite:expired-token")).thenReturn(null)

        val exception = assertThrows(ResponseStatusException::class.java) {
            service.enterByInvite("expired-token")
        }

        assertEquals(HttpStatus.GONE, exception.statusCode)
        assertEquals("INVITE_EXPIRED", exception.reason)
    }
}

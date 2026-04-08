package com.example.chatService.service

import com.example.chatService.dto.RoomRole
import com.example.chatService.entity.ChatRoom
import com.example.chatService.entity.RoomParticipant
import com.example.chatService.event.RoomParticipantsChangedEvent
import com.example.chatService.exception.BannedFromRoomException
import com.example.chatService.repository.ChatRoomV2Repository
import com.example.chatService.repository.RoomParticipantRepository
import com.example.chatService.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class RoomParticipantServiceImplTest {
    private val roomParticipantRepository: RoomParticipantRepository = mock()
    private val userRepository: UserRepository = mock()
    private val redis: StringRedisTemplate = mock()
    private val roomRepository: ChatRoomV2Repository = mock()
    private val publisher: ParticipantEventPublisherImpl = mock()
    private val messagingTemplate: SimpMessagingTemplate = mock()
    private val eventPublisher: ApplicationEventPublisher = mock()

    private val service = RoomParticipantServiceImpl(
        roomParticipantRepository,
        userRepository,
        redis,
        roomRepository,
        publisher,
        messagingTemplate,
        eventPublisher,
    )

    @Test
    fun `joinRoom throws when user is banned from room`() {
        whenever(roomParticipantRepository.existsByRoomIdAndUserIdAndIsBannedTrue("room-1", "user-1")).thenReturn(true)

        assertThrows(BannedFromRoomException::class.java) {
            service.joinRoom("room-1", "user-1")
        }

        verify(roomRepository, never()).findByIdForUpdate(any())
    }

    @Test
    fun `joinRoom activates inactive participant and publishes changed event`() {
        val participant = RoomParticipant(roomId = "room-1", userId = "user-1", role = RoomRole.MEMBER).apply {
            isActive = false
        }
        val room = ChatRoom.create("room", com.example.chatService.dto.RoomType.PUBLIC, 10, "owner-1").apply {
            roomId = "room-1"
            currentCount = 0
        }
        val eventCaptor = argumentCaptor<RoomParticipantsChangedEvent>()

        whenever(roomParticipantRepository.existsByRoomIdAndUserIdAndIsBannedTrue("room-1", "user-1")).thenReturn(false)
        whenever(roomParticipantRepository.findByRoomIdAndUserId("room-1", "user-1")).thenReturn(Optional.of(participant))
        whenever(roomRepository.findByIdForUpdate("room-1")).thenReturn(room)
        whenever(roomParticipantRepository.save(participant)).thenReturn(participant)

        service.joinRoom("room-1", "user-1")

        assertTrue(participant.isActive)
        assertEquals(1, room.currentCount)
        verify(roomParticipantRepository, times(1)).save(participant)
        verify(eventPublisher).publishEvent(eventCaptor.capture())
        assertEquals("room-1", eventCaptor.firstValue.roomId)
    }

    @Test
    fun `leaveRoom deactivates active participant and decrements room count`() {
        val participant = RoomParticipant(roomId = "room-1", userId = "user-1", role = RoomRole.MEMBER).apply {
            isActive = true
        }
        val room = ChatRoom.create("room", com.example.chatService.dto.RoomType.PUBLIC, 10, "owner-1").apply {
            roomId = "room-1"
            currentCount = 2
        }

        whenever(roomParticipantRepository.findByRoomIdAndUserId("room-1", "user-1")).thenReturn(Optional.of(participant))
        whenever(roomRepository.findByIdForUpdate("room-1")).thenReturn(room)
        whenever(roomParticipantRepository.save(participant)).thenReturn(participant)

        service.leaveRoom("room-1", "user-1")

        assertFalse(participant.isActive)
        assertEquals(1, room.currentCount)
        verify(eventPublisher).publishEvent(any<RoomParticipantsChangedEvent>())
    }
}

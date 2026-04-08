package com.example.chatService.service

import com.example.chatService.dto.JoinByInviteResponse
import com.example.chatService.dto.RoomRole
import com.example.chatService.dto.RoomType
import com.example.chatService.repository.ChatRoomV2Repository
import com.example.chatService.repository.RoomParticipantRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.util.UUID

@Service
@Transactional
class RoomInviteService(
    private val redis: StringRedisTemplate,
    private val roomRepository: ChatRoomV2Repository,
    private val roomParticipantService: RoomParticipantService,
    private val participantRepository: RoomParticipantRepository,
) {
    fun joinByInvite(inviteCode: String, userId: String): JoinByInviteResponse {
        val key = "room:invite:$inviteCode"
        val roomId = redis.opsForValue().get(key)
            ?: throw IllegalArgumentException("초대 코드가 유효하지 않습니다.")

        val room = roomRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("방이 존재하지 않습니다.") }

        if (room.type != RoomType.PRIVATE) {
            throw IllegalStateException("비밀방이 아닙니다")
        }

        roomParticipantService.joinRoom(room.roomId, userId)
        return JoinByInviteResponse(room.roomId)
    }

    fun generateInviteCode(roomId: String, userId: String): String {
        if (
            !participantRepository.existsByRoomIdAndUserIdAndRoleIn(
                roomId,
                userId,
                listOf(RoomRole.OWNER, RoomRole.ADMIN),
            )
        ) {
            throw AccessDeniedException("권한 없음")
        }

        val room = roomRepository.findById(roomId)
            .orElseThrow { IllegalStateException("방 없음") }

        if (room.type != RoomType.PRIVATE) {
            throw IllegalStateException("비밀방 아님")
        }

        val inviteCode = UUID.randomUUID().toString()
        redis.opsForValue().set("room:invite:$inviteCode", roomId, INVITE_TTL)
        return inviteCode
    }

    fun enterByInvite(token: String): String =
        redis.opsForValue().get("room:invite:$token")
            ?: throw ResponseStatusException(HttpStatus.GONE, "INVITE_EXPIRED")

    companion object {
        private val INVITE_TTL: Duration = Duration.ofMinutes(10)
    }
}

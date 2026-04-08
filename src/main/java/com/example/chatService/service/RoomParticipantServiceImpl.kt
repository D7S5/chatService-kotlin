package com.example.chatService.service

import com.example.chatService.dto.AdminChangedResponse
import com.example.chatService.dto.ParticipantDto
import com.example.chatService.dto.RoomCountDto
import com.example.chatService.dto.RoomRole
import com.example.chatService.dto.RoomRole.ADMIN
import com.example.chatService.dto.RoomRole.MEMBER
import com.example.chatService.dto.RoomRole.OWNER
import com.example.chatService.entity.ChatRoom
import com.example.chatService.entity.RoomParticipant
import com.example.chatService.event.ParticipantForcedExitEvent
import com.example.chatService.event.RoomParticipantsChangedEvent
import com.example.chatService.exception.BannedFromRoomException
import com.example.chatService.repository.ChatRoomV2Repository
import com.example.chatService.repository.RoomParticipantRepository
import com.example.chatService.repository.UserRepository
import jakarta.persistence.OptimisticLockException
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.retry.annotation.Retryable
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
@Transactional
class RoomParticipantServiceImpl(

    private val roomParticipantRepository: RoomParticipantRepository,
    private val userRepository: UserRepository,
    private val redis: StringRedisTemplate,
    private val roomRepository: ChatRoomV2Repository,
    private val publisher: ParticipantEventPublisherImpl,
    private val messagingTemplate: SimpMessagingTemplate,
    private val eventPublisher: ApplicationEventPublisher,

) : RoomParticipantService {
    @Retryable(value = [OptimisticLockException::class], maxAttempts = 3)
    @Transactional
    override fun joinRoom(roomId: String, userId: String) {
        if (roomParticipantRepository.existsByRoomIdAndUserIdAndIsBannedTrue(roomId, userId)) {
            throw BannedFromRoomException(roomId)
        }

        val role = hasPermission(roomId, userId)
        joinAsRole(roomId, userId, role)
    }

    fun hasPermission(roomId: String, userId: String): RoomRole {
        val member = roomParticipantRepository.findByRoomIdAndUserId(roomId, userId)
        if (member.isEmpty) {
            return MEMBER
        }
        return member.get().role ?: MEMBER
    }

    @Transactional
    fun joinAsRole(roomId: String, userId: String, roomRole: RoomRole) {
        val participant = roomParticipantRepository.findByRoomIdAndUserId(roomId, userId)
            .orElseGet {
                roomParticipantRepository.save(
                    RoomParticipant(roomId = roomId, userId = userId, role = roomRole),
                )
            }

        if (participant.isActive) {
            return
        }

        val room = roomRepository.findByIdForUpdate(roomId)
        room.increaseCount()
        participant.activate()
        roomParticipantRepository.save(participant)

        eventPublisher.publishEvent(RoomParticipantsChangedEvent(roomId))
    }

    @Transactional
    override fun leaveRoom(roomId: String, userId: String) {
        val participant = getParticipant(roomId, userId)
        if (!participant.isActive) {
            return
        }

        val room = roomRepository.findByIdForUpdate(roomId)
        room.decreaseCount()
        participant.deactivate()
        roomParticipantRepository.save(participant)
        eventPublisher.publishEvent(RoomParticipantsChangedEvent(roomId))
    }

    override fun reconnect(roomId: String, userId: String) {
    }

    @Transactional
    override fun kick(roomId: String, targetUserId: String, byUserId: String) {
        requireAdmin(roomId, byUserId)
        if (byUserId == targetUserId) {
            throw IllegalStateException("Cannot kick yourself")
        }

        val target = getParticipant(roomId, targetUserId)
        if (target.role == RoomRole.OWNER) {
            throw IllegalStateException("OWNER는 강퇴할 수 없습니다.")
        }
        if (!target.isActive) {
            return
        }

        target.deactivate()
        roomParticipantRepository.save(target)

        val room = roomRepository.findByIdForUpdate(roomId)
        room.decreaseCount()

        eventPublisher.publishEvent(ParticipantForcedExitEvent(roomId, targetUserId, "KICK"))
        eventPublisher.publishEvent(RoomParticipantsChangedEvent(roomId))
    }

    @Transactional
    override fun ban(roomId: String, targetUserId: String, byUserId: String, reason: String) {
        requireOwner(roomId, byUserId)
        if (targetUserId == byUserId) {
            throw IllegalStateException("Cannot ban yourself")
        }

        val target = roomParticipantRepository.findByRoomIdAndUserId(roomId, targetUserId)
            .orElseThrow { IllegalStateException("Target not in room") }

        if (target.role == RoomRole.OWNER) {
            throw IllegalStateException("Cannot ban OWNER")
        }

        val wasActive = target.isActive
        target.ban(reason)

        if (wasActive) {
            val room = roomRepository.findByIdForUpdate(roomId)
            room.decreaseCount()
            roomParticipantRepository.save(target)

            eventPublisher.publishEvent(ParticipantForcedExitEvent(roomId, targetUserId, reason))
            eventPublisher.publishEvent(RoomParticipantsChangedEvent(roomId))
        }
    }

    @Transactional
    override fun changeRole(roomId: String, targetUserId: String, role: RoomRole, byUserId: String) {
        validateOwner(roomId, byUserId)
        val target = getParticipant(roomId, targetUserId)
        target.changeRole(role)
        roomParticipantRepository.save(target)
    }

    @Transactional
    override fun toggleAdmin(roomId: String, requesterId: String, targetUserId: String): AdminChangedResponse {
        val requester = roomParticipantRepository.findByRoomIdAndUserId(roomId, requesterId).orElseThrow()
        if (requester.role != RoomRole.OWNER) {
            throw AccessDeniedException("OWNER만 가능")
        }

        val target = roomParticipantRepository.findByRoomIdAndUserId(roomId, targetUserId).orElseThrow()
        if (target.role == RoomRole.OWNER) {
            throw IllegalStateException("OWNER는 변경 불가")
        }

        target.role = if (target.role == ADMIN) MEMBER else ADMIN
        roomParticipantRepository.save(target)

        return AdminChangedResponse(targetUserId, target.role?.name ?: MEMBER.name)
    }

    @Transactional
    override fun transferOwnership(roomId: String, newOwnerId: String, byUserId: String) {
        requireOwner(roomId, byUserId)
        val room = roomRepository.findByIdForUpdate(roomId)

        if (room.ownerUserId != byUserId) {
            throw SecurityException("Owner only")
        }
        if (byUserId == newOwnerId) {
            return
        }

        room.ownerUserId = newOwnerId
        roomRepository.save(room)
        publisher.broadcastOwnerChanged(roomId, newOwnerId)
    }

    private fun requireOwner(roomId: String, userId: String) {
        val room = roomRepository.findById(roomId).orElseThrow()
        if (room.ownerUserId != userId) {
            throw SecurityException("OWNER only")
        }
    }

    private fun requireAdmin(roomId: String, userId: String) {
        val participant = roomParticipantRepository.findByRoomIdAndUserId(roomId, userId)
            .orElseThrow { AccessDeniedException("Not in room") }

        if (participant.role == RoomRole.MEMBER) {
            throw AccessDeniedException("ADMIN only")
        }
    }

    @Transactional(readOnly = true)
    override fun getActiveParticipants(roomId: String): List<RoomParticipant> =
        roomParticipantRepository.findAllByRoomIdAndIsActiveTrue(roomId)

    override fun getParticipants(roomId: String): List<ParticipantDto> =
        getActiveParticipants(roomId).map {
            ParticipantDto(
                it.userId,
                loadUsername(it.userId),
                it.role ?: MEMBER,
            )
        }

    private fun getParticipant(roomId: String, userId: String): RoomParticipant =
        roomParticipantRepository.findByRoomIdAndUserId(roomId, userId)
            .orElseThrow {
                IllegalStateException("Participant not found. roomId=$roomId, userId=$userId")
            }

    private fun validateOwner(roomId: String, userId: String) {
        val participant = getParticipant(roomId, userId)
        if (participant.role != OWNER) {
            throw SecurityException("Owner only")
        }
    }

    @Transactional
    override fun getCurrentCount(roomId: String): Int =
        roomRepository.findById(roomId).orElseThrow().currentCount

    private fun loadUsername(userId: String?): String {
        if (userId == null) {
            return "UNKNOWN"
        }

        val key = "user:$userId:username"
        val cached = redis.opsForValue().get(key)
        if (cached != null) {
            return cached
        }

        val fromDb = userRepository.findUsernameValueById(userId)
        if (fromDb == null) {
            log.warn("Username not found for userId={}", userId)
            return "UNKNOWN"
        }

        redis.opsForValue().set(key, fromDb, Duration.ofHours(1))
        return fromDb
    }

    @Transactional
    override fun broadcast(roomId: String) {
        val current = getCurrentCount(roomId)
        val room: ChatRoom = roomRepository.findById(roomId)
            .orElseThrow { RuntimeException("broadcast") }

        messagingTemplate.convertAndSend("/topic/room-users/$roomId", "UPDATED")
        messagingTemplate.convertAndSend(
            "/topic/rooms/$roomId/count",
            RoomCountDto(current, room.maxParticipants),
        )
    }

    override fun isParticipant(roomId: String, userId: String): Boolean =
        roomParticipantRepository.existsByRoomIdAndUserId(roomId, userId)

    companion object {
        private val log = LoggerFactory.getLogger(RoomParticipantServiceImpl::class.java)
    }
}

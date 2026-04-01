package com.example.chatService.service

import com.example.chatService.dto.CreateRoomRequest
import com.example.chatService.dto.RoomResponse
import com.example.chatService.dto.RoomRole
import com.example.chatService.dto.RoomType
import com.example.chatService.entity.ChatRoom
import com.example.chatService.entity.RoomParticipant
import com.example.chatService.repository.ChatRoomV2Repository
import com.example.chatService.repository.RoomParticipantRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.UUID

@Service
class ChatRoomService(
        private val redis : StringRedisTemplate,
        private val chatRoomV2Repository: ChatRoomV2Repository,
        private val repository: RoomParticipantRepository,
        private val service : RoomParticipantService,
        private val inviteService: RoomInviteService
) {
    fun getAllRooms() : List<ChatRoom> {
        return  chatRoomV2Repository.findAll();
    }

    @Transactional
    fun create(req : CreateRoomRequest, userId : String) : RoomResponse {
        if ( req.maxParticipants < 2) {
            throw IllegalArgumentException("최소 인원은 2명입니다.")
        }

        val room = ChatRoom.create(
                req.name,
                req.type,
                req.maxParticipants,
                userId
        )

        chatRoomV2Repository.save(room)

        repository.save(
                RoomParticipant(
                        roomId = room.roomId,
                        userId = userId,
                        role = RoomRole.OWNER,
                        isActive = true
                ))
        var inviteToken: String? = null;

        if (req.type == RoomType.PRIVATE) {
            inviteToken = UUID.randomUUID().toString()

            redis.opsForValue().set(
                    "room:invite:$inviteToken",
                    room.roomId,
                    Duration.ofMinutes(10)
            )
            inviteService.joinByInvite(inviteToken, userId)
        }

        return RoomResponse.of(room, inviteToken)
    }

    fun getRoom(roomId : String, userId: String): RoomResponse {
        val room = chatRoomV2Repository.findById(roomId)
                .orElseThrow { IllegalArgumentException("Room not found")}

        if (room.type == RoomType.PRIVATE) {
            val joined = service.isParticipant(roomId, userId)
            if (!joined) {
                return RoomResponse.inaccessible(room, "PRIVATE_ROOM")
            }
        }
        return RoomResponse.from(room)
    }

    fun joinRoom(roomId: String, userId: String) {

        chatRoomV2Repository.findById(roomId)
                .orElseThrow { IllegalArgumentException("Room not found") }
        service.joinRoom(roomId, userId)
    }


}
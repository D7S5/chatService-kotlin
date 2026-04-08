package com.example.chatService.service

import com.example.chatService.dto.DMRoomDto
import com.example.chatService.entity.DMMessage
import com.example.chatService.entity.DMRoom
import com.example.chatService.repository.DMMessageRepository
import com.example.chatService.repository.DMRoomRepository
import com.example.chatService.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
@Transactional
class DMService(
        private val roomRepository : DMRoomRepository,
        private val dmMessageRepository : DMMessageRepository,
        private val userRepository : UserRepository
) {
    fun startOrGetRoom(userA: String, userB : String) : DMRoom {
        return roomRepository.findByUserAIdAndUserBId(userA, userB)
                .or { roomRepository.findByUserBIdAndUserAId(userA, userB) }
                .orElseGet {
                    val room = DMRoom(
                            roomId = UUID.randomUUID().toString(),
                            userAId = userA,
                            userBId = userB
                    )
                    roomRepository.save(room);
                }
    }
    fun getMessages(roomId : String) : List<DMMessage> {
        val room = roomRepository.findById(roomId)
                .orElseThrow { RuntimeException("Room not found") }
        return dmMessageRepository.findByRoomOrderBySentAtAsc(room);
    }

    fun sendMessage(roomId: String, senderId: String, content : String) : DMMessage {
        val room = roomRepository.findById(roomId)
                .orElseThrow { RuntimeException("Room not found") }

        val msg = DMMessage(
                room = room,
                senderId = senderId,
                content = content,
                sentAt = OffsetDateTime.now(),
                isRead = false
        )
        room.lastMessageTime = OffsetDateTime.now()
        roomRepository.save(room)

        return dmMessageRepository.save(msg)
    }

    fun getReceiverId(roomId : String, senderId : String) : String? {
        val room = roomRepository.findById(roomId)
                .orElseThrow { IllegalArgumentException("Room not found") }

        return if (room.userAId == senderId) {
            room.userBId
        } else {
            room.userAId
        }
    }

    fun getUserRoomsWithUnread(myUserId : String) : List<DMRoomDto> {
        val rooms = roomRepository.findByUser(myUserId)
        return rooms.map { room ->
            val targetUserId = if (room.userAId == myUserId) room.userBId else room.userAId

            val targetUser = userRepository.findById(targetUserId ?: throw RuntimeException("user not found"))
                    .orElseThrow { RuntimeException("user not found") }
            val unread = dmMessageRepository.countUnread(
                    room.roomId ?: throw RuntimeException("room id not found"),
                    myUserId
            )

            DMRoomDto(
                    room.roomId,
                    targetUser.id,
                    targetUser.username,
                    unread)
        }
    }
    @Transactional
    fun markMessagesAsRead(roomId : String, userId: String) {
        dmMessageRepository.markAsReadByRoomAndReceiver(roomId, userId)
    }
}

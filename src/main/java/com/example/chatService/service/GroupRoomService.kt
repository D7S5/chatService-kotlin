package com.example.chatService.service

import com.example.chatService.dto.ChatMessageResponse
import com.example.chatService.dto.RoomResponse
import com.example.chatService.repository.ChatRoomV2Repository
import com.example.chatService.repository.GroupMessageRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class GroupRoomService  (
    private val chatRoomV2Repository : ChatRoomV2Repository,
    private val groupMessageRepository : GroupMessageRepository ) {

    fun messages(roomId : String, limit : Int): List<ChatMessageResponse> {
        val size = minOf(limit, 200)

        val entities = groupMessageRepository.findRecent(
                roomId,
                PageRequest.of(0, size)
        ).toMutableList()

        entities.reverse()

        return entities.map(ChatMessageResponse::from)
    }

    fun getRoomsWithCount(): List<RoomResponse> {
        return chatRoomV2Repository.findAll()
                .map(RoomResponse::from)
    }
}
package com.example.chatService.service

import com.example.chatService.component.WebSocketEventPublisher
import com.example.chatService.dto.*
import com.example.chatService.entity.Friend
import com.example.chatService.entity.User
import com.example.chatService.repository.FriendRepository
import com.example.chatService.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class FriendService (
        private val userRepository: UserRepository,
        private val friendRepository: FriendRepository,
        private val eventPublisher: WebSocketEventPublisher
){

    @Transactional
    fun sendFriendRequest(fromUserId : String, toUserId : String) : FriendRequestResponseDto {
        val from = userRepository.findById(fromUserId)
                .orElseThrow { IllegalArgumentException("from user not found") }

        val to = userRepository.findById(toUserId)
                .orElseThrow { IllegalArgumentException("Target user not found")}

        if (from.id.equals(to.id)) {
            throw IllegalArgumentException("자기 자신에게 보낼 수 없습니다.")
        }

        if (friendRepository.existsByUserAndFriend(from, to) || friendRepository.existsByUserAndFriend(to, from)) {
            throw IllegalArgumentException("이미 요청을 보냈거나 친구입니다.")
        }

        val f = Friend(
                user = from,
                friend = to,
                status = FriendStatus.PENDING
        )
        friendRepository.save(f)

        val publishEvent = PublishFriendEvent(
                type = FriendEventType.FRIEND_REQUEST,
                fromUserId = fromUserId,
                fromUserNickname = from.usernameValue
        )

        eventPublisher.publishFriendEvent(toUserId, publishEvent)

        return FriendRequestResponseDto(
                fromUserId = fromUserId,
                toUserId = toUserId,
                status = f.status!!.name
        )
    }

    fun getReceivedRequests(userId: String): List<FriendRequestDto> {
        val requests = friendRepository.findPendingRequestsReceivedByUserId(userId)

        return requests.map { relation ->
            val fromUser = relation.user
                    ?: throw IllegalArgumentException("친구 요청 보낸 사용자가 없습니다")
            val toUser = relation.friend
                    ?: throw IllegalArgumentException("친구 요청 대상 사용자가 없습니다")
            val status = relation.status
                    ?: throw IllegalArgumentException("친구 요청 상태가 없습니다")

            FriendRequestDto(
                    id = relation.id,
                    toUserId = toUser.id,
                    fromUserNickname = fromUser.usernameValue,
                    fromUserId = fromUser.id,
                    status = status
            )
        }
    }

    @Transactional(readOnly = true)
    fun getFriendList(userId: String) : List<User> {
        return friendRepository.findAllRelatedWithFetch(userId)
                .filter { f -> f.status == FriendStatus.ACCEPTED }
                .mapNotNull { relation ->
                    val user = relation.user
                    val friend = relation.friend

                    when {
                        user?.id == userId && friend != null -> friend
                        friend?.id == userId && user != null -> user
                        else -> null
                    }
                }.distinctBy { it.id }
    }

    @Transactional
    fun removeFriend(me : String, target: String) {
        val userA = userRepository.findById(me)
                .orElseThrow { IllegalArgumentException("user not found, me = $me target id = $target") }
        val userB = userRepository.findById(target)
                .orElseThrow { IllegalArgumentException("user not found, me = $me target id = $target") }

        val friends = friendRepository.findFriendRelation(userA, userB)

        friendRepository.deleteAll(friends)
    }

    @Transactional
    fun acceptFriendRequest(requestId : Long) : String {
        val request = friendRepository.findById(requestId)
                .orElseThrow { IllegalArgumentException("요청을 찾을 수 없습니다.") }

        if ( request.status != FriendStatus.PENDING) {
            throw IllegalArgumentException("이미 처리된 요청입니다.")
        }

        val sender = request.user
                ?: throw IllegalArgumentException("요청 보낸 사용자가 없습니다")
        val receiver = request.friend
                ?: throw IllegalArgumentException("요청 받은 사용자가 없습니다")

        request.status = FriendStatus.ACCEPTED
        friendRepository.save(request)

        val friendA =  Friend(
                user = sender,
                friend = receiver,
                status = FriendStatus.ACCEPTED
        )

        val friendB = Friend(
                user = receiver,
                friend = sender,
                status = FriendStatus.ACCEPTED
        )

        friendRepository.save(friendA)
        friendRepository.save(friendB)

        val publishAcceptFriendEvent = PublishAcceptFriendEvent(
                type = FriendEventType.FRIEND_ACCEPT,
                friendId = receiver.id
        )

        eventPublisher.publishAcceptFriendEvent(
                sender.id,
                publishAcceptFriendEvent
        )
        return "친구 수락 완료"
    }

    @Transactional
    fun reject(requestId: Long) : String {
        val req = friendRepository.findById(requestId)
                .orElseThrow { IllegalArgumentException("요청 없음") }

        if (req.status != FriendStatus.PENDING)
            throw IllegalArgumentException("이미 처리됨")

        req.status = FriendStatus.BLOCKED
        return "거절 완료"
    }
}
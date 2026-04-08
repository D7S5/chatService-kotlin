package com.example.chatService.repository

import com.example.chatService.dto.FriendStatus
import com.example.chatService.entity.Friend
import com.example.chatService.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FriendRepository : JpaRepository<Friend, Long> {
    fun existsByUserAndFriend(user: User, friend: User): Boolean

    fun findByFriend_Id(friendId: String): List<Friend>

    @Query(
        """
            SELECT f FROM Friend f
            WHERE (f.user = :me AND f.friend = :target)
               OR (f.user = :target AND f.friend = :me)
        """,
    )
    fun findFriendRelation(@Param("me") me: User, @Param("target") target: User): List<Friend>

    @Query("SELECT f FROM Friend f JOIN FETCH f.user u WHERE f.friend.id = :userId AND f.status = 'PENDING'")
    fun findPendingRequestsReceivedByUserId(@Param("userId") userId: String): List<Friend>

    @Query("SELECT f FROM Friend f JOIN FETCH f.friend u WHERE f.user.id = :userId AND f.status = 'PENDING'")
    fun findPendingRequestsSentByUserId(@Param("userId") userId: String): List<Friend>

    @Query(
        "SELECT f FROM Friend f " +
            "LEFT JOIN FETCH f.user " +
            "LEFT JOIN FETCH f.friend " +
            "WHERE f.user.id = :userId OR f.friend.id = :userId",
    )
    fun findAllRelatedWithFetch(@Param("userId") userId: String): List<Friend>

    fun findByUserId(userId: String): List<Friend>

    @Query("SELECT f FROM Friend f JOIN FETCH f.user WHERE f.friend.id = :userId AND f.status = 'PENDING'")
    fun findReceivedRequests(@Param("userId") userId: String): List<Friend>

    fun findByFriendId(friendId: String): List<Friend>

    @Query("SELECT f FROM Friend f WHERE f.user.id = :userId OR f.friend.id = :userId")
    fun findByUserIdOrFriendId(@Param("userId") userId: String): List<Friend>

    @Query("SELECT f FROM Friend f WHERE (f.user.id = :userId OR f.friend.id = :userId) AND f.status = :status")
    fun findByUserIdOrFriendIdAndStatus(@Param("userId") userId: String, @Param("status") status: FriendStatus): List<Friend>

    @Query("SELECT f FROM Friend f JOIN FETCH f.user JOIN FETCH f.friend WHERE f.user = :user OR f.friend = :user")
    fun findAllRelatedFriends(@Param("user") user: User): List<Friend>

    @Query("SELECT f FROM Friend f JOIN FETCH f.user JOIN FETCH f.friend WHERE f.user = :user")
    fun findByUser(@Param("user") user: User): List<Friend>

    @Query("SELECT f FROM Friend f JOIN FETCH f.user JOIN FETCH f.friend WHERE f.friend = :user")
    fun findByFriend(@Param("user") user: User): List<Friend>

    fun existsByUserIdAndFriendId(userId: String, targetUserId: String): Boolean

    fun existsByUserIdAndFriendIdOrUserIdAndFriendId(
        userId1: String,
        friendId1: String,
        userId2: String,
        friendId2: String,
    ): Boolean
}

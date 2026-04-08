package com.example.chatService.repository

import com.example.chatService.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface UserRepository : JpaRepository<User, String> {
    fun existsByUsernameValue(username: String): Boolean

    fun existsByEmail(email: String): Boolean

    fun findByEmail(email: String): Optional<User>

    @Query("select u.usernameValue from User u where u.id = :userId")
    fun findUsernameValueById(userId: String): String?
}

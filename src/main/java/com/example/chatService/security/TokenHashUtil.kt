package com.example.chatService.security

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64

object TokenHashUtil {
    @JvmStatic
    fun hash(token: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(token.toByteArray(StandardCharsets.UTF_8))
            Base64.getEncoder().encodeToString(hash)
        } catch (e: Exception) {
            throw RuntimeException("Token hashing failed", e)
        }
    }
}

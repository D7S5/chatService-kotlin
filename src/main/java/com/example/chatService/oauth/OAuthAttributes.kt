package com.example.chatService.oauth

import com.example.chatService.entity.AuthProvider
import com.example.chatService.entity.Role
import com.example.chatService.entity.User

class OAuthAttributes(
        val attributes: Map<String, Any>,
        val nameAttributeKey: String,
        val email: String,
        val username: String,
        val providerId: String,
        val provider: AuthProvider
) {

    fun toEntity(): User {
        return User(
                email = email,
                usernameValue = username,
                provider = provider,
                providerId = providerId,
                role = Role.USER,
                nicknameCompleted = false
        )
    }

    companion object {
        fun of(
                registrationId: String,
                userNameAttributeName: String,
                attributes: Map<String, Any>
        ): OAuthAttributes {
            return when (registrationId) {
                "google" -> ofGoogle(userNameAttributeName, attributes)
                "naver" -> ofNaver(userNameAttributeName, attributes)
                "kakao" -> ofKakao(userNameAttributeName, attributes)
                else -> throw IllegalArgumentException(
                        "Unsupported OAuth provider: $registrationId"
                )
            }
        }

        private fun ofGoogle(
                userNameAttributeName: String,
                attributes: Map<String, Any>
        ): OAuthAttributes {
            val providerId = attributes["sub"] as? String
                    ?: throw IllegalArgumentException("Google providerId(Sub) is null")

            if (providerId.isBlank()) {
                throw IllegalArgumentException("Google providerId(Sub) is null")
            }

            val email = attributes["email"] as? String

            val safeEmail = email
                    ?: "${AuthProvider.GOOGLE.name.lowercase()}_${providerId.substring(0, 8)}@oauth.local"

            var username = attributes["name"] as? String

            if (username.isNullOrBlank()) {
                username = "google_$providerId"
            }

            return OAuthAttributes(
                    provider = AuthProvider.GOOGLE,
                    providerId = providerId,
                    email = safeEmail,
                    username = username,
                    attributes = attributes,
                    nameAttributeKey = userNameAttributeName
            )
        }

        private fun ofNaver(
                userNameAttributeName: String,
                attributes: Map<String, Any>
        ): OAuthAttributes {
            val response = attributes["response"] as? Map<String, Any>
                    ?: throw IllegalArgumentException("Naver response is null")

            val providerId = response["id"] as? String
                    ?: throw IllegalArgumentException("Naver providerId(id) is null")

            if (providerId.isBlank()) {
                throw IllegalArgumentException("Naver providerId(id) is null")
            }

            val email = response["email"] as? String
            val safeEmail = email
                    ?: "${AuthProvider.NAVER.name.lowercase()}_${providerId.substring(0, 8)}@oauth.local"

            var username = response["name"] as? String

            if (username.isNullOrBlank()) {
                username = "naver_$providerId"
            }

            return OAuthAttributes(
                    provider = AuthProvider.NAVER,
                    providerId = providerId,
                    email = safeEmail,
                    username = username,
                    attributes = attributes,
                    nameAttributeKey = userNameAttributeName
            )
        }

        private fun ofKakao(
                userNameAttributeName: String,
                attributes: Map<String, Any>
        ): OAuthAttributes {
            val kakaoAccount = attributes["kakao_account"] as? Map<String, Any>
                    ?: throw IllegalArgumentException("Kakao account is null")

            val profile = kakaoAccount["profile"] as? Map<String, Any>

            val rawId = attributes["id"]
                    ?: throw IllegalArgumentException("Kakao providerId(id) is null")
            val providerId = rawId.toString()

            val email = kakaoAccount["email"] as? String

            var username: String? = null

            if (profile != null) {
                username = profile["nickname"] as? String
            }

            if (username.isNullOrBlank()) {
                username = "kakao_$providerId"
            }

            val safeEmail = email
                    ?: "${AuthProvider.KAKAO.name.lowercase()}_${providerId.substring(0, 8)}@oauth.local"

            return OAuthAttributes(
                    provider = AuthProvider.KAKAO,
                    providerId = providerId,
                    email = safeEmail,
                    username = username,
                    attributes = attributes,
                    nameAttributeKey = userNameAttributeName
            )
        }
    }
}
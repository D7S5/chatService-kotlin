package com.example.chatService.oauth

import com.example.chatService.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

class CustomOAuth2User(
    private val user: User,
    private val attributes: Map<String, Any>,
) : OAuth2User {
    fun getUser(): User = user

    override fun getAttributes(): Map<String, Any> = attributes

    override fun getAuthorities(): Collection<GrantedAuthority> = user.authorities

    override fun getName(): String = user.id ?: ""
}

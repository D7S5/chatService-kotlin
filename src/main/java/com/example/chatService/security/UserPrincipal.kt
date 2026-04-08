package com.example.chatService.security

import com.example.chatService.entity.AuthProvider
import com.example.chatService.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User

class UserPrincipal private constructor(
    val id: String,
    private val email: String?,
    private val username: String?,
    private val password: String?,
    val isNicknameCompleted: Boolean,
    val provider: AuthProvider?,
    val providerId: String?,
    private val authorities: Collection<GrantedAuthority>,
    private var attributes: Map<String, Any>? = null
) : UserDetails, OAuth2User {

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities
    override fun getPassword(): String? = password
    override fun getUsername(): String? = username
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
    override fun getAttributes(): Map<String, Any>? = attributes

    override fun getName(): String {
        return if (!email.isNullOrBlank()) email
        else "${provider?.name?.lowercase()}_$providerId"
    }

    companion object {
        @JvmStatic
        fun from(user: User): UserPrincipal {
            val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role}"))
            return UserPrincipal(
                id = user.id ?: "",
                email = user.email,
                username = user.username,
                password = user.password,
                isNicknameCompleted = user.nicknameCompleted,
                provider = user.provider,
                providerId = user.providerId,
                authorities = authorities
            )
        }

        @JvmStatic
        fun create(user: User): UserPrincipal {
            val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
            return UserPrincipal(
                id = user.id ?: "",
                email = user.email,
                username = user.username,
                password = user.password,
                isNicknameCompleted = user.nicknameCompleted,
                provider = user.provider,
                providerId = user.providerId,
                authorities = authorities
            )
        }

        @JvmStatic
        fun create(user: User, attributes: Map<String, Any>): UserPrincipal {
            return create(user).also { it.attributes = attributes }
        }
    }
}

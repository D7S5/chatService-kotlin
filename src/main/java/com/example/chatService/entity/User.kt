package com.example.chatService.entity

import com.example.chatService.oauth.OAuthAttributes
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

@Entity
@Table(name = "users")
class User(

        @Id
        @Column(length = 36, nullable = false)
        var id: String = UUID.randomUUID().toString(),

        @Column(nullable = true, unique = true)
        var usernameValue: String? = null,

        @Column(nullable = false)
        var nicknameCompleted: Boolean = false,

        var passwordValue: String? = null,

        @Column(nullable = false, unique = true)
        var email: String? = null,

        @Column(nullable = false)
        var online: Boolean = false,

        @Enumerated(EnumType.STRING)
        var provider: AuthProvider? = null,

        var providerId: String? = null,

        @Column(nullable = false)
        @Enumerated(EnumType.STRING)
        var role: Role? = null

) : UserDetails {

    constructor(
            username: String,
            password: String,
            email: String,
            role: Role
    ) : this(
            usernameValue = username,
            passwordValue = password,
            email = email,
            role = role
    )

    fun updateOAuth(attributes: OAuthAttributes): User {
        this.usernameValue = attributes.username
        this.provider = attributes.provider
        this.providerId = attributes.providerId
        return this
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${role}"))
    }

    override fun getPassword(): String? = passwordValue ?: ""

    override fun getUsername(): String? = email ?: ""

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    companion object {
        fun oauthUser(
                email: String,
                name: String,
                provider: AuthProvider,
                providerId: String
        ): User {
            return User().apply {
                this.email = email
                this.usernameValue = name
                this.passwordValue = null
                this.role = Role.USER
                this.provider = provider
                this.providerId = providerId
            }
        }
    }
}
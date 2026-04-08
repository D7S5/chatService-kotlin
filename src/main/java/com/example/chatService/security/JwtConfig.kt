package com.example.chatService.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "jwt")
class JwtConfig {
    var secret: String? = null
        get() = field?.trim()
    var accessTokenExpiry: Long = 7_200_000
    var refreshTokenExpiry: Long = 604_800_000
}

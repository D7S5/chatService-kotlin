package com.example.chatService.oauth

import com.example.chatService.entity.User
import com.example.chatService.repository.UserRepository
import com.example.chatService.security.UserPrincipal
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
) : DefaultOAuth2UserService() {
    override fun loadUser(request: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(request)
        val registrationId = request.clientRegistration.registrationId
        val userNameAttributeName = request.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName

        val attributes = OAuthAttributes.of(
            registrationId,
            userNameAttributeName,
            oAuth2User.attributes,
        )

        val user: User = userRepository.findByEmail(attributes.email)
            .map { entity -> entity.updateOAuth(attributes) }
            .orElseGet { userRepository.save(attributes.toEntity()) }

        return UserPrincipal.create(user, oAuth2User.attributes)
    }
}

package com.example.chatService.controller

import com.example.chatService.dto.NicknameDto
import com.example.chatService.entity.AuthProvider
import com.example.chatService.entity.Role
import com.example.chatService.entity.User
import com.example.chatService.oauth.CustomOAuth2UserService
import com.example.chatService.oauth.OAuth2FailureHandler
import com.example.chatService.oauth.OAuth2SuccessHandler
import com.example.chatService.security.JwtAuthenticationFilter
import com.example.chatService.security.UserPrincipal
import com.example.chatService.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.mockito.kotlin.whenever

@WebMvcTest(UserController::class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerWebMvcTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var userService: UserService

    @MockitoBean
    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    @MockitoBean
    private lateinit var customOAuth2UserService: CustomOAuth2UserService

    @MockitoBean
    private lateinit var oAuth2SuccessHandler: OAuth2SuccessHandler

    @MockitoBean
    private lateinit var oAuth2FailureHandler: OAuth2FailureHandler

    @Test
    fun `setNickname returns trimmed nickname`() {
        val principal = userPrincipal(id = "user-1", email = "user1@example.com")
        whenever(userService.setNickname("user-1", "  Austin  ")).thenReturn("Austin")
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(principal, null, principal.authorities)

        try {
            mockMvc.perform(
                post("/api/user/set-nickname")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("nickname" to "  Austin  "))),
            )
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nickname").value("Austin"))
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    private fun userPrincipal(id: String, email: String): UserPrincipal =
        UserPrincipal.from(
            User(
                id = id,
                usernameValue = "tester",
                nicknameCompleted = true,
                passwordValue = "encoded",
                email = email,
                online = false,
                provider = AuthProvider.LOCAL,
                providerId = null,
                role = Role.USER,
            ),
        )
}

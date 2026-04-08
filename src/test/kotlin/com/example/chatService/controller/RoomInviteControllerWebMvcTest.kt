package com.example.chatService.controller

import com.example.chatService.dto.JoinByInviteResponse
import com.example.chatService.dto.RoomResponse
import com.example.chatService.dto.RoomType
import com.example.chatService.entity.AuthProvider
import com.example.chatService.entity.Role
import com.example.chatService.entity.User
import com.example.chatService.oauth.CustomOAuth2UserService
import com.example.chatService.oauth.OAuth2FailureHandler
import com.example.chatService.oauth.OAuth2SuccessHandler
import com.example.chatService.security.JwtAuthenticationFilter
import com.example.chatService.security.UserPrincipal
import com.example.chatService.service.ChatRoomService
import com.example.chatService.service.RoomInviteService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(RoomInviteController::class)
@AutoConfigureMockMvc(addFilters = false)
class RoomInviteControllerWebMvcTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var inviteService: RoomInviteService

    @MockitoBean
    private lateinit var chatRoomService: ChatRoomService

    @MockitoBean
    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    @MockitoBean
    private lateinit var customOAuth2UserService: CustomOAuth2UserService

    @MockitoBean
    private lateinit var oAuth2SuccessHandler: OAuth2SuccessHandler

    @MockitoBean
    private lateinit var oAuth2FailureHandler: OAuth2FailureHandler

    @Test
    fun `joinByInvite delegates to service with authenticated user`() {
        val principal = userPrincipal(id = "user-1", email = "user1@example.com")
        whenever(inviteService.joinByInvite("invite-123", "user-1"))
            .thenReturn(JoinByInviteResponse("room-1"))
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(principal, null, principal.authorities)

        try {
            mockMvc.perform(
                post("/api/rooms/join-by-invite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mapOf("inviteCode" to "invite-123"))),
            )
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.roomId").value("room-1"))
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    @Test
    fun `getRoom returns room response for authenticated user`() {
        val principal = userPrincipal(id = "user-1", email = "user1@example.com")
        whenever(chatRoomService.getRoom("room-1", "user-1"))
            .thenReturn(
                RoomResponse(
                    roomId = "room-1",
                    name = "Secret Room",
                    type = RoomType.PRIVATE,
                    currentCount = 2,
                    maxParticipants = 10,
                    accessible = true,
                    reason = null,
                    largeRoom = false,
                    ownerUserId = "owner-1",
                    inviteToken = null,
                ),
            )
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(principal, null, principal.authorities)

        try {
            mockMvc.perform(get("/api/rooms/room-1"))
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.roomId").value("room-1"))
                .andExpect(jsonPath("$.name").value("Secret Room"))
                .andExpect(jsonPath("$.type").value("PRIVATE"))
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

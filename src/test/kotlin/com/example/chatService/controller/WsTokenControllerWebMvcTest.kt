package com.example.chatService.controller

import com.example.chatService.oauth.CustomOAuth2UserService
import com.example.chatService.oauth.OAuth2FailureHandler
import com.example.chatService.oauth.OAuth2SuccessHandler
import com.example.chatService.redis.WsTokenService
import com.example.chatService.security.CookieUtil
import com.example.chatService.security.JwtAuthenticationFilter
import com.example.chatService.security.JwtTokenProvider
import org.junit.jupiter.api.Test
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(WsTokenController::class)
@AutoConfigureMockMvc(addFilters = false)
class WsTokenControllerWebMvcTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var wsTokenService: WsTokenService

    @MockitoBean
    private lateinit var cookieUtil: CookieUtil

    @MockitoBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @MockitoBean
    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    @MockitoBean
    private lateinit var customOAuth2UserService: CustomOAuth2UserService

    @MockitoBean
    private lateinit var oAuth2SuccessHandler: OAuth2SuccessHandler

    @MockitoBean
    private lateinit var oAuth2FailureHandler: OAuth2FailureHandler

    @Test
    fun `createWsToken returns unauthorized when refresh token missing`() {
        whenever(cookieUtil.getRefreshToken(org.mockito.kotlin.any())).thenReturn(null)

        mockMvc.perform(post("/api/ws/token"))
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.wsToken").doesNotExist())
            .andExpect(jsonPath("$.expiresIn").value(0))

        verify(jwtTokenProvider, never()).validateToken(org.mockito.kotlin.any())
    }

    @Test
    fun `createWsToken returns ws token for valid refresh token`() {
        whenever(cookieUtil.getRefreshToken(org.mockito.kotlin.any())).thenReturn("refresh-token")
        whenever(jwtTokenProvider.validateToken("refresh-token")).thenReturn(true)
        whenever(jwtTokenProvider.getSubject("refresh-token")).thenReturn("user-1")
        whenever(wsTokenService.createTokenForUser("user-1")).thenReturn("ws-token-1")

        mockMvc.perform(post("/api/ws/token"))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.wsToken").value("ws-token-1"))
            .andExpect(jsonPath("$.expiresIn").value(120))
    }
}

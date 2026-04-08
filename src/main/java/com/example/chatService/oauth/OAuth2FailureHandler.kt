package com.example.chatService.oauth

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class OAuth2FailureHandler : SimpleUrlAuthenticationFailureHandler() {
    @Throws(IOException::class)
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException,
    ) {
        log.error("OAuth2 Login Failed")
        log.error("Exception class: {}", exception::class.java.name)
        log.error("Message: {}", exception.message, exception)
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.message)
    }

    companion object {
        private val log = LoggerFactory.getLogger(OAuth2FailureHandler::class.java)
    }
}

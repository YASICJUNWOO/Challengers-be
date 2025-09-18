package com.habitchallenge.presentation.controller

import com.habitchallenge.application.service.AuthService
import com.habitchallenge.domain.user.Role
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.mockito.BDDMockito.given

@WebMvcTest(AuthController::class)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var authService: AuthService

    @Test
    fun `회원가입 성공`() {
        // Given
        val request = mapOf(
            "email" to "test@example.com",
            "password" to "password123",
            "nickname" to "testuser",
            "role" to "USER"
        )

        val response = AuthService.AuthResponse(
            token = "jwt-token",
            user = AuthService.UserResponse(
                id = 1L,
                email = "test@example.com",
                nickname = "testuser",
                role = "USER",
                profileImageUrl = null
            )
        )

        given(authService.signup("test@example.com", "password123", "testuser", Role.USER))
            .willReturn(response)

        // When & Then
        mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.user.email").value("test@example.com"))
            .andExpect(jsonPath("$.user.nickname").value("testuser"))
            .andExpect(jsonPath("$.user.role").value("USER"))
    }

    @Test
    fun `잘못된 이메일 형식으로 회원가입 실패`() {
        // Given
        val request = mapOf(
            "email" to "invalid-email",
            "password" to "password123",
            "nickname" to "testuser",
            "role" to "USER"
        )

        // When & Then
        mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `로그인 성공`() {
        // Given
        val request = mapOf(
            "email" to "test@example.com",
            "password" to "password123"
        )

        val response = AuthService.AuthResponse(
            token = "jwt-token",
            user = AuthService.UserResponse(
                id = 1L,
                email = "test@example.com",
                nickname = "testuser",
                role = "USER",
                profileImageUrl = null
            )
        )

        given(authService.signin("test@example.com", "password123"))
            .willReturn(response)

        // When & Then
        mockMvc.perform(
            post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.user.email").value("test@example.com"))
    }
}
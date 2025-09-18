package com.habitchallenge.application.service

import com.habitchallenge.domain.user.Role
import com.habitchallenge.domain.user.User
import com.habitchallenge.domain.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        passwordEncoder = mockk()
        userService = UserService(userRepository, passwordEncoder)
    }

    @Test
    fun `사용자 생성 성공`() {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val nickname = "testuser"
        val encodedPassword = "encodedPassword"

        every { userRepository.existsByEmail(email) } returns false
        every { userRepository.existsByNickname(nickname) } returns false
        every { passwordEncoder.encode(password) } returns encodedPassword
        every { userRepository.save(any()) } returns mockk<User> {
            every { id } returns 1L
            every { this@mockk.email } returns email
            every { this@mockk.nickname } returns nickname
            every { role } returns Role.USER
        }

        // When
        val result = userService.createUser(email, password, nickname)

        // Then
        assertNotNull(result)
        verify { userRepository.existsByEmail(email) }
        verify { userRepository.existsByNickname(nickname) }
        verify { passwordEncoder.encode(password) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun `이미 존재하는 이메일로 사용자 생성 실패`() {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val nickname = "testuser"

        every { userRepository.existsByEmail(email) } returns true

        // When & Then
        assertThrows<IllegalArgumentException> {
            userService.createUser(email, password, nickname)
        }
    }

    @Test
    fun `이미 존재하는 닉네임으로 사용자 생성 실패`() {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val nickname = "testuser"

        every { userRepository.existsByEmail(email) } returns false
        every { userRepository.existsByNickname(nickname) } returns true

        // When & Then
        assertThrows<IllegalArgumentException> {
            userService.createUser(email, password, nickname)
        }
    }

    @Test
    fun `사용자 조회 성공`() {
        // Given
        val userId = 1L
        val user = mockk<User> {
            every { id } returns userId
            every { email } returns "test@example.com"
            every { nickname } returns "testuser"
        }

        every { userRepository.findById(userId) } returns Optional.of(user)

        // When
        val result = userService.findById(userId)

        // Then
        assertEquals(user, result)
        verify { userRepository.findById(userId) }
    }

    @Test
    fun `존재하지 않는 사용자 조회 실패`() {
        // Given
        val userId = 1L
        every { userRepository.findById(userId) } returns Optional.empty()

        // When & Then
        assertThrows<NoSuchElementException> {
            userService.findById(userId)
        }
    }
}
package com.habitchallenge.infrastructure.config

import com.habitchallenge.application.service.UserService
import jakarta.transaction.Transactional
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataInitializer(
    private val userService: UserService
) {
    @Bean
    fun initUsers(): ApplicationRunner = ApplicationRunner {
        createTestUsers()
    }

    data class TestUser(
        val email: String,
        val username: String,
        val password: String,
        val nickname: String
    )

    @Transactional
    fun createTestUsers() {
        val users = listOf(
            TestUser("junwoo@gmail.com", "junwoo", "123456789", "준우"),
            TestUser("hwi@gmail.com","hwi", "123456789", "휘진"),
            TestUser("mason@gmail.com","mason", "123456789", "메이슨"),
            TestUser("someone@gmail.com","someone", "123456789", "홍길동")
        )
        users.forEach { (email, loginId, password, nickname) ->
            try {
                userService.createUser(email, loginId, password, nickname)
                println("✅ 테스트 유저 생성 완료: $loginId ($nickname)")
            } catch (e: IllegalArgumentException) {
                println("ℹ️ 이미 존재하는 유저: $loginId")
            }
        }
    }
}
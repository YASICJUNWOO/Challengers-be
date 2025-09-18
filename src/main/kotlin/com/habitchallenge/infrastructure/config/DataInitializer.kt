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

    @Transactional
    fun createTestUsers() {
        val users = listOf(
            Triple("junwoo", "123456789", "준우"),
            Triple("hwi", "123456789", "휘진"),
            Triple("mason", "123456789", "메이슨")
        )

        users.forEach { (loginId, password, nickname) ->
            try {
                userService.createUser(loginId, password, nickname)
                println("✅ 테스트 유저 생성 완료: $loginId ($nickname)")
            } catch (e: IllegalArgumentException) {
                println("ℹ️ 이미 존재하는 유저: $loginId")
            }
        }
    }
}
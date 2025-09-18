package com.habitchallenge

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class HabitChallengeApplicationTest {

    @Test
    fun `애플리케이션 컨텍스트 로드 테스트`() {
        // Spring Boot 애플리케이션이 정상적으로 로드되는지 확인
    }
}
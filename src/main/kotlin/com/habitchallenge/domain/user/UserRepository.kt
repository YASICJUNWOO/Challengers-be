package com.habitchallenge.domain.user

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByLoginId(loginId: String): Optional<User>
    fun findByEmail(email: String): Optional<User>
    fun findByGoogleId(googleId: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun existsByLoginId(loginId: String): Boolean
    fun existsByNickname(nickname: String): Boolean
    fun existsByGoogleId(googleId: String): Boolean
    fun findByRole(role: UserRole, pageable: Pageable): Page<User>
}
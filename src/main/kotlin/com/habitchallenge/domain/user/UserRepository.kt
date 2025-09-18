package com.habitchallenge.domain.user

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByLoginId(loginId: String): Optional<User>
    fun existsByLoginId(loginId: String): Boolean
    fun existsByNickname(nickname: String): Boolean
    fun findByRole(role: UserRole, pageable: Pageable): Page<User>
}
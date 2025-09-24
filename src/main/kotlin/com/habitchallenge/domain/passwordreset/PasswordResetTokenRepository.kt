package com.habitchallenge.domain.passwordreset

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, Long> {
    fun findByEmailAndCodeAndIsUsedFalseAndExpiresAtAfter(
        email: String,
        code: String,
        now: LocalDateTime
    ): Optional<PasswordResetToken>

    fun findByResetTokenAndIsUsedFalse(resetToken: String): Optional<PasswordResetToken>

    fun findByEmailAndIsUsedFalse(email: String): List<PasswordResetToken>

    fun countByEmailAndCreatedAtAfter(email: String, after: LocalDateTime): Long
}
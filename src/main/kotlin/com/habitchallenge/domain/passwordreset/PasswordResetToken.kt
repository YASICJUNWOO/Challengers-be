package com.habitchallenge.domain.passwordreset

import com.habitchallenge.domain.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "password_reset_tokens")
class PasswordResetToken(
    @Column(nullable = false)
    val email: String,

    @Column(nullable = false, length = 6)
    val code: String,

    @Column(nullable = false, name = "expires_at")
    var expiresAt: LocalDateTime,

    @Column(nullable = false)
    var isUsed: Boolean = false,

    @Column(name = "reset_token", unique = true)
    var resetToken: String? = null
) : BaseEntity() {

    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }

    fun markAsUsed() {
        this.isUsed = true
    }

    fun updateResetToken(token: String) {
        this.resetToken = token
    }

    fun extendExpiry(newExpiresAt: LocalDateTime) {
        this.expiresAt = newExpiresAt
    }
}
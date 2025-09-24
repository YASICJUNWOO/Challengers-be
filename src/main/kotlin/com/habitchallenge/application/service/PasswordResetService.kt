package com.habitchallenge.application.service

import com.habitchallenge.domain.passwordreset.PasswordResetToken
import com.habitchallenge.domain.passwordreset.PasswordResetTokenRepository
import com.habitchallenge.domain.user.AuthProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional(readOnly = true)
class PasswordResetService(
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val emailService: EmailService
) {

    private val secureRandom = SecureRandom()

    companion object {
        private const val CODE_EXPIRY_MINUTES = 10L
        private const val TOKEN_EXPIRY_MINUTES = 30L
        private const val MAX_REQUESTS_PER_HOUR = 5
    }

    /**
     * 비밀번호 재설정 인증코드 발송
     */
    @Transactional
    fun sendResetCode(email: String) {
        // 1. 사용자 존재 여부 확인
        val user = userService.findByEmail(email)
            ?: throw NoSuchElementException("등록되지 않은 이메일입니다.")

        // 2. 소셜 로그인 사용자 제외
        if (user.authProvider != AuthProvider.LOCAL) {
            throw IllegalArgumentException("소셜 로그인 계정은 비밀번호 재설정을 할 수 없습니다.")
        }

        // 3. 요청 빈도 제한 확인 (1시간 내 5회 제한)
        val oneHourAgo = LocalDateTime.now().minusHours(1)
        val recentRequestsCount = passwordResetTokenRepository.countByEmailAndCreatedAtAfter(email, oneHourAgo)

        if (recentRequestsCount >= MAX_REQUESTS_PER_HOUR) {
            throw IllegalStateException("너무 많은 요청을 보냈습니다. 잠시 후 다시 시도해주세요.")
        }

        // 4. 기존 미사용 토큰들 비활성화
        val existingTokens = passwordResetTokenRepository.findByEmailAndIsUsedFalse(email)
        existingTokens.forEach { it.markAsUsed() }
        passwordResetTokenRepository.saveAll(existingTokens)

        // 5. 새 인증코드 생성 및 저장
        val code = generateSixDigitCode()
        val expiresAt = LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES)

        val resetToken = PasswordResetToken(
            email = email,
            code = code,
            expiresAt = expiresAt
        )

        passwordResetTokenRepository.save(resetToken)

        // 6. 이메일 발송
        emailService.sendPasswordResetCode(email, user.nickname, code)
    }

    /**
     * 인증코드 검증 및 임시 토큰 발급
     */
    @Transactional
    fun verifyCodeAndGenerateToken(email: String, code: String): String {
        // 1. 유효한 인증코드 확인
        val resetToken = passwordResetTokenRepository
            .findByEmailAndCodeAndIsUsedFalseAndExpiresAtAfter(email, code, LocalDateTime.now())
            .orElseThrow { IllegalArgumentException("유효하지 않거나 만료된 인증코드입니다.") }

        // 2. 임시 토큰 생성 및 저장
        val tempToken = generateSecureToken()
        resetToken.updateResetToken(tempToken)

        // 3. 인증코드 만료 시간 연장 (임시 토큰 유효 시간)
        resetToken.extendExpiry(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES))

        passwordResetTokenRepository.save(resetToken)

        return tempToken
    }

    /**
     * 임시 비밀번호 발급 및 비밀번호 재설정 완료
     */
    @Transactional
    fun confirmResetAndGenerateTemporaryPassword(token: String): String {
        // 1. 유효한 토큰 확인
        val resetToken = passwordResetTokenRepository
            .findByResetTokenAndIsUsedFalse(token)
            .orElseThrow { IllegalArgumentException("유효하지 않거나 만료된 토큰입니다.") }

        if (resetToken.isExpired()) {
            throw IllegalArgumentException("만료된 토큰입니다.")
        }

        // 2. 임시 비밀번호 생성
        val temporaryPassword = generateTemporaryPassword()

        // 3. 사용자 비밀번호 업데이트
        val user = userService.findByEmail(resetToken.email)!!
        val encodedPassword = passwordEncoder.encode(temporaryPassword)
        userService.updateUserPassword(user.id!!, encodedPassword)

        // 4. 토큰 사용 완료 처리
        resetToken.markAsUsed()
        passwordResetTokenRepository.save(resetToken)

        // 5. 이메일 발송
        emailService.sendTemporaryPassword(resetToken.email, user.nickname, temporaryPassword)

        return temporaryPassword
    }

    private fun generateSixDigitCode(): String {
        return String.format("%06d", secureRandom.nextInt(1000000))
    }

    private fun generateSecureToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun generateTemporaryPassword(): String {
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val length = 10

        return (1..length)
            .map { characters[secureRandom.nextInt(characters.length)] }
            .joinToString("")
    }
}
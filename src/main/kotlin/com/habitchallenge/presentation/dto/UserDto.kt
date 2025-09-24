package com.habitchallenge.presentation.dto

import com.habitchallenge.domain.user.User
import com.habitchallenge.domain.user.UserRole
import jakarta.validation.constraints.*
import java.time.format.DateTimeFormatter

data class UserResponse(
    val id: String,
    val name: String,        // User.nickname 필드 매핑
    val email: String?,      // User.email 필드 추가
    val loginId: String,
    val avatar: String?,     // User.avatarUrl 필드 매핑
    val role: String,        // 'leader' 또는 'member'
    val loginType: String,   // 'local' 또는 'social'
    val provider: String?,   // 'google' 또는 null (for local)
    val createdAt: String    // ISO 날짜 형식
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id.toString(),
                name = user.nickname,
                email = user.email,
                loginId = user.loginId,
                avatar = user.avatarUrl,
                role = when (user.role) {
                    UserRole.LEADER -> "leader"
                    UserRole.MEMBER -> "member"
                },
                loginType = user.loginType,
                provider = user.provider,
                createdAt = user.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
        }
    }
}

data class SignupRequest(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "유효한 이메일 형식이어야 합니다.")
    val email: String,

    @field:NotBlank(message = "로그인 ID는 필수입니다.")
    @field:Size(min = 2, max = 20, message = "로그인 ID는 2자 이상 20자 이하여야 합니다.")
    @field:Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "로그인 ID는 영문, 숫자, 언더스코어, 하이픈만 사용 가능합니다.")
    val loginId: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Size(min = 8, max = 50, message = "비밀번호는 8자 이상 50자 이하여야 합니다.")
    val password: String,

    @field:NotBlank(message = "닉네임은 필수입니다.")
    @field:Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    val nickname: String,

    @field:Pattern(regexp = "LEADER|MEMBER", message = "역할은 LEADER 또는 MEMBER만 가능합니다.")
    val role: String = "MEMBER"
)

data class SigninRequest(
    @field:NotBlank(message = "로그인 ID는 필수입니다.")
    val id: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: UserResponse
)

data class GoogleLoginRequest(
    @field:NotBlank(message = "Google 토큰은 필수입니다.")
    val token: String
)

// Profile update request
data class UpdateUserRequest(
    @field:Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    val nickname: String?,

    @field:Email(message = "유효한 이메일 형식이어야 합니다.")
    val email: String?
)

// Password change request
data class ChangePasswordRequest(
    @field:NotBlank(message = "현재 비밀번호는 필수입니다.")
    val currentPassword: String,

    @field:NotBlank(message = "새 비밀번호는 필수입니다.")
    @field:Size(min = 8, max = 50, message = "비밀번호는 8자 이상 50자 이하여야 합니다.")
    val newPassword: String
)

// Password reset request - step 1
data class PasswordResetRequest(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "유효한 이메일 형식이어야 합니다.")
    val email: String
)

// Password reset verify - step 2
data class PasswordResetVerifyRequest(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "유효한 이메일 형식이어야 합니다.")
    val email: String,

    @field:NotBlank(message = "인증번호는 필수입니다.")
    @field:Size(min = 6, max = 6, message = "인증번호는 6자리여야 합니다.")
    val code: String
)

// Password reset confirm - step 3
data class PasswordResetConfirmRequest(
    @field:NotBlank(message = "토큰은 필수입니다.")
    val token: String
)

// Password reset responses
data class PasswordResetResponse(
    val message: String,
    val success: Boolean = true
)

data class PasswordResetVerifyResponse(
    val message: String,
    val token: String,
    val success: Boolean = true
)

data class PasswordResetConfirmResponse(
    val message: String,
    val temporaryPassword: String,
    val success: Boolean = true
)
package com.habitchallenge.presentation.dto

import com.habitchallenge.domain.challengeapplication.ApplicationStatus
import com.habitchallenge.domain.challengeapplication.ChallengeApplication
import jakarta.validation.constraints.*
import java.time.format.DateTimeFormatter

data class ChallengeApplicationResponse(
    val id: String,
    val challengeId: String,
    val userId: String,
    val reason: String,
    val status: String,
    val createdAt: String,
    val reviewedAt: String?,
    val rejectionReason: String?,
    val user: EmbeddedUserInfo
) {
    companion object {
        fun from(application: ChallengeApplication): ChallengeApplicationResponse {
            return ChallengeApplicationResponse(
                id = application.id.toString(),
                challengeId = application.challenge.id.toString(),
                userId = application.user.id.toString(),
                reason = application.reason,
                status = when (application.status) {
                    ApplicationStatus.PENDING -> "pending"
                    ApplicationStatus.APPROVED -> "approved"
                    ApplicationStatus.REJECTED -> "rejected"
                },
                createdAt = application.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                reviewedAt = application.reviewedAt?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                rejectionReason = application.rejectionReason,
                user = EmbeddedUserInfo(
                    id = application.user.id.toString(),
                    name = application.user.nickname,
                    avatar = application.user.avatarUrl
                )
            )
        }
    }
}

data class EmbeddedUserInfo(
    val id: String,
    val name: String,
    val avatar: String?
)

data class CreateApplicationRequest(
    @field:NotBlank(message = "신청 사유는 필수입니다.")
    @field:Size(min = 10, max = 500, message = "신청 사유는 10자 이상 500자 이하여야 합니다.")
    val reason: String
)

data class UpdateApplicationStatusRequest(
    @field:NotBlank(message = "상태는 필수입니다.")
    @field:Pattern(
        regexp = "approved|rejected",
        message = "상태는 'approved' 또는 'rejected'만 가능합니다."
    )
    val status: String,

    @field:Size(min = 10, max = 500, message = "반려 사유는 10자 이상 500자 이하여야 합니다.")
    val rejectionReason: String? = null
) {
    fun validate() {
        if (status == "rejected" && rejectionReason.isNullOrBlank()) {
            throw IllegalArgumentException("반려 시 반려 사유는 필수입니다.")
        }
    }

    fun toApplicationStatus(): ApplicationStatus {
        return when (status) {
            "approved" -> ApplicationStatus.APPROVED
            "rejected" -> ApplicationStatus.REJECTED
            else -> throw IllegalArgumentException("Unknown application status: $status")
        }
    }
}
package com.habitchallenge.presentation.dto

import com.habitchallenge.domain.challengelog.ChallengeLog
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.format.DateTimeFormatter

data class ChallengeLogResponse(
    val id: String,
    val userId: String,
    val challengeId: String,
    val content: String,
    val imageUrl: String?,
    val status: String,              // 소문자 변환
    val rejectionComment: String?,
    val user: UserInfo,              // 임베디드 사용자 정보
    val createdAt: String            // ISO 날짜 형식
) {
    data class UserInfo(
        val id: String,
        val name: String,
        val avatar: String?
    ) {
        companion object {
            fun from(user: com.habitchallenge.domain.user.User): UserInfo {
                return UserInfo(
                    id = user.id.toString(),
                    name = user.nickname,
                    avatar = user.avatarUrl
                )
            }
        }
    }

    companion object {
        fun from(log: ChallengeLog): ChallengeLogResponse {
            return ChallengeLogResponse(
                id = log.id.toString(),
                userId = log.user.id.toString(),
                challengeId = log.challenge.id.toString(),
                content = log.content,
                imageUrl = log.imageUrl,
                status = log.status.name.lowercase(),
                rejectionComment = log.rejectionComment,
                user = UserInfo.from(log.user),
                createdAt = log.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
        }
    }
}

data class CreateChallengeLogRequest(
    @field:NotNull(message = "챌린지 ID는 필수입니다.")
    val challengeId: String,

    @field:NotBlank(message = "내용은 필수입니다.")
    @field:Size(min = 10, max = 2000, message = "내용은 10자 이상 2000자 이하여야 합니다.")
    val content: String,

    val imageUrl: String?            // 선택적 이미지 URL
)

data class ReviewChallengeLogRequest(
    @field:Size(max = 500, message = "코멘트는 500자 이하여야 합니다.")
    val comment: String?
)
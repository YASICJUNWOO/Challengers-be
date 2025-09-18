package com.habitchallenge.presentation.dto

import com.habitchallenge.domain.challenge.ChallengeCategory
import com.habitchallenge.domain.challenge.ChallengeDifficulty
import com.habitchallenge.domain.challenge.Challenge
import com.habitchallenge.domain.challenge.LeaderRole
import jakarta.validation.constraints.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ChallengeResponse(
    val id: String,
    val name: String,
    val description: String,
    val category: String,        // 소문자 변환
    val difficulty: String,      // 소문자 변환
    val duration: Int,
    val startDate: String,       // ISO 날짜 형식
    val endDate: String,         // ISO 날짜 형식
    val maxMembers: Int,
    val currentMembers: Int,     // 계산된 값
    val leaderId: String,
    val leader: UserResponse,    // 임베디드 리더 정보
    val members: List<UserResponse>, // 참여자 목록
    val status: String,          // 소문자 변환
    val coverImage: String?,     // CoverImageUrl 필드 매핑
    val reward: String?,
    val tags: List<String>,
    val createdAt: String,       // ISO 날짜 형식
    val isPrivate: Boolean?,     // 비공개 챌린지 여부
    val inviteCode: String?,     // 초대 코드 (비공개일 경우)
    val leaderRole: String?      // 리더 역할 (participant/manager)
) {
    companion object {
        fun from(
            challenge: Challenge,
            currentMemberCount: Long,
            members: List<UserResponse>
        ): ChallengeResponse {
            return ChallengeResponse(
                id = challenge.id.toString(),
                name = challenge.name,
                description = challenge.description,
                category = challenge.category.name.lowercase(),
                difficulty = challenge.difficulty.name.lowercase(),
                duration = challenge.duration,
                startDate = challenge.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                endDate = challenge.endDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                maxMembers = challenge.maxMembers,
                currentMembers = currentMemberCount.toInt(),
                leaderId = challenge.leader.id.toString(),
                leader = UserResponse.from(challenge.leader),
                members = members,
                status = challenge.status.name.lowercase(),
                coverImage = challenge.coverImageUrl,
                reward = challenge.reward,
                tags = challenge.tags.toList(),
                createdAt = challenge.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                isPrivate = challenge.isPrivate,
                inviteCode = challenge.inviteCode,
                leaderRole = challenge.leaderRole.name.lowercase()
            )
        }
    }
}

data class CreateChallengeRequest(
    @field:NotBlank(message = "이름은 필수입니다.")
    @field:Size(min = 2, max = 100, message = "이름은 2자 이상 100자 이하여야 합니다.")
    val name: String,

    @field:NotBlank(message = "설명은 필수입니다.")
    @field:Size(min = 10, max = 1000, message = "설명은 10자 이상 1000자 이하여야 합니다.")
    val description: String,

    @field:NotBlank(message = "카테고리는 필수입니다.")
    @field:Pattern(
        regexp = "health|study|habit|hobby|social|business",
        message = "카테고리는 health, study, habit, hobby, social, business 중 하나여야 합니다."
    )
    val category: String,

    @field:NotBlank(message = "난이도는 필수입니다.")
    @field:Pattern(
        regexp = "easy|medium|hard",
        message = "난이도는 easy, medium, hard 중 하나여야 합니다."
    )
    val difficulty: String,

    @field:NotNull(message = "기간은 필수입니다.")
    @field:Min(value = 1, message = "기간은 1일 이상이어야 합니다.")
    @field:Max(value = 365, message = "기간은 365일 이하여야 합니다.")
    val duration: Int,

    @field:NotNull(message = "시작일은 필수입니다.")
    val startDate: String,       // ISO 날짜 형식

    @field:NotNull(message = "종료일은 필수입니다.")
    val endDate: String,         // ISO 날짜 형식

    @field:NotNull(message = "최대 참여자 수는 필수입니다.")
    @field:Min(value = 2, message = "최대 참여자 수는 2명 이상이어야 합니다.")
    @field:Max(value = 1000, message = "최대 참여자 수는 1000명 이하여야 합니다.")
    val maxMembers: Int,

    val coverImage: String?,     // 선택적 커버 이미지 URL

    @field:Size(max = 500, message = "보상은 500자 이하여야 합니다.")
    val reward: String?,

    val tags: List<String> = emptyList(),

    val isPrivate: Boolean = false,  // 비공개 챌린지 여부 (기본값: false)

    @field:Pattern(
        regexp = "participant|manager",
        message = "리더 역할은 participant 또는 manager여야 합니다."
    )
    val leaderRole: String = "participant"  // 리더 역할 (기본값: participant)
) {
    fun toEntity(leader: com.habitchallenge.domain.user.User, inviteCode: String? = null): Challenge {
        return Challenge(
            name = name,
            description = description,
            category = ChallengeCategory.valueOf(category.uppercase()),
            difficulty = ChallengeDifficulty.valueOf(difficulty.uppercase()),
            duration = duration,
            startDate = LocalDate.parse(startDate),
            endDate = LocalDate.parse(endDate),
            maxMembers = maxMembers,
            leader = leader,
            coverImageUrl = coverImage,
            reward = reward,
            tags = tags.toSet(),
            isPrivate = isPrivate,
            inviteCode = inviteCode,
            leaderRole = LeaderRole.valueOf(leaderRole.uppercase())
        )
    }
}

data class JoinChallengeRequest(
    @field:Size(max = 500, message = "참여 이유는 500자 이하여야 합니다.")
    val joinReason: String?
)

data class JoinChallengeByUserIdRequest(
    @field:NotBlank(message = "사용자 ID는 필수입니다.")
    val userId: String,

    @field:Size(max = 500, message = "참여 이유는 500자 이하여야 합니다.")
    val joinReason: String?
)

data class JoinChallengeByInviteCodeRequest(
    @field:NotBlank(message = "초대 코드는 필수입니다.")
    @field:Size(min = 8, max = 8, message = "초대 코드는 8자리여야 합니다.")
    val inviteCode: String,

    @field:Size(max = 500, message = "참여 이유는 500자 이하여야 합니다.")
    val joinReason: String?
)

data class UpdateChallengeRequest(
    @field:Size(min = 2, max = 100, message = "이름은 2자 이상 100자 이하여야 합니다.")
    val name: String?,

    @field:Size(min = 10, max = 1000, message = "설명은 10자 이상 1000자 이하여야 합니다.")
    val description: String?,

    @field:Pattern(
        regexp = "health|study|habit|hobby|social|business",
        message = "카테고리는 health, study, habit, hobby, social, business 중 하나여야 합니다."
    )
    val category: String?,

    @field:Pattern(
        regexp = "easy|medium|hard",
        message = "난이도는 easy, medium, hard 중 하나여야 합니다."
    )
    val difficulty: String?,

    @field:Min(value = 1, message = "기간은 1일 이상이어야 합니다.")
    @field:Max(value = 365, message = "기간은 365일 이하여야 합니다.")
    val duration: Int?,

    val startDate: String?,       // ISO 날짜 형식

    val endDate: String?,         // ISO 날짜 형식

    @field:Min(value = 2, message = "최대 참여자 수는 2명 이상이어야 합니다.")
    @field:Max(value = 1000, message = "최대 참여자 수는 1000명 이하여야 합니다.")
    val maxMembers: Int?,

    val coverImage: String?,     // 선택적 커버 이미지 URL

    @field:Size(max = 500, message = "보상은 500자 이하여야 합니다.")
    val reward: String?,

    val tags: List<String>?
)
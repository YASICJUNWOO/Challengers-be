package com.habitchallenge.application.service

import com.habitchallenge.domain.challenge.*
import com.habitchallenge.domain.challengeapplication.ApplicationStatus
import com.habitchallenge.domain.challengeapplication.ChallengeApplication
import com.habitchallenge.domain.challengeapplication.ChallengeApplicationRepository
import com.habitchallenge.domain.participation.Participation
import com.habitchallenge.domain.participation.ParticipationRepository
import com.habitchallenge.domain.participation.ParticipationStatus
import com.habitchallenge.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.random.Random

@Service
@Transactional(readOnly = true)
class ChallengeService(
    private val challengeRepository: ChallengeRepository,
    private val participationRepository: ParticipationRepository,
    private val challengeApplicationRepository: ChallengeApplicationRepository,
    private val challengeLogRepository: com.habitchallenge.domain.challengelog.ChallengeLogRepository,
    private val notificationService: NotificationService,
    private val userService: UserService
) {

    @Transactional
    fun createChallenge(
        name: String,
        description: String,
        category: ChallengeCategory,
        difficulty: ChallengeDifficulty,
        duration: Int,
        startDate: LocalDate,
        endDate: LocalDate,
        maxMembers: Int,
        leader: User,
        coverImageUrl: String? = null,
        reward: String? = null,
        tags: Set<String> = emptySet(),
        isPrivate: Boolean = false,
        leaderRole: LeaderRole = LeaderRole.PARTICIPANT
    ): Challenge {
        validateChallengeData(startDate, endDate, maxMembers)

        // Generate invite code for private challenges
        val inviteCode = if (isPrivate) generateInviteCode() else null

        val challenge = Challenge(
            name = name,
            description = description,
            category = category,
            difficulty = difficulty,
            duration = duration,
            startDate = startDate,
            endDate = endDate,
            maxMembers = maxMembers,
            leader = leader,
            coverImageUrl = coverImageUrl,
            reward = reward,
            tags = tags,
            isPrivate = isPrivate,
            inviteCode = inviteCode,
            leaderRole = leaderRole
        )

        val savedChallenge = challengeRepository.save(challenge)

        // Auto-add leader as member if leaderRole is PARTICIPANT
        if (leaderRole == LeaderRole.PARTICIPANT) {
            val participation = Participation(
                user = leader,
                challenge = savedChallenge,
                status = ParticipationStatus.JOINED,
                joinReason = "리더 자동 참여"
            )
            participationRepository.save(participation)
        }

        return savedChallenge
    }

    fun findById(id: Long): Challenge {
        return challengeRepository.findById(id)
            .orElseThrow { NoSuchElementException("챌린지 그룹을 찾을 수 없습니다: $id") }
    }

    fun findAll(pageable: Pageable): Page<Challenge> {
        // Only return public challenges in the general listing
        return challengeRepository.findByIsPrivate(false, pageable)
    }

    fun findByCategory(category: ChallengeCategory, pageable: Pageable): Page<Challenge> {
        return challengeRepository.findByCategoryAndIsPrivate(category, false, pageable)
    }

    fun findByStatus(status: ChallengeStatus, pageable: Pageable): Page<Challenge> {
        return challengeRepository.findByStatusAndIsPrivate(status, false, pageable)
    }

    fun findByCategoryAndStatus(
        category: ChallengeCategory,
        status: ChallengeStatus,
        pageable: Pageable
    ): Page<Challenge> {
        return challengeRepository.findByCategoryAndStatusAndIsPrivate(category, status, false, pageable)
    }

    fun searchChallenges(search: String, status: ChallengeStatus?, pageable: Pageable): Page<Challenge> {
        return if (status != null) {
            challengeRepository.findByStatusAndSearchAndIsPrivate(status, search, false, pageable)
        } else {
            challengeRepository.findBySearchAndIsPrivate(search, false, pageable)
        }
    }

    @Transactional
    fun updateChallenge(
        challengeId: Long,
        currentUser: User,
        name: String?,
        description: String?,
        category: ChallengeCategory?,
        difficulty: ChallengeDifficulty?,
        duration: Int?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        maxMembers: Int?,
        coverImageUrl: String?,
        reward: String?,
        tags: Set<String>?
    ): Challenge {
        val existingChallenge = findById(challengeId)

        // 권한 체크 - 그룹 리더만 수정 가능
        if (existingChallenge.leader.id != currentUser.id) {
            throw IllegalArgumentException("그룹 수정 권한이 없습니다.")
        }

        // Validate the updated data if provided
        val finalStartDate = startDate ?: existingChallenge.startDate
        val finalEndDate = endDate ?: existingChallenge.endDate
        val finalMaxMembers = maxMembers ?: existingChallenge.maxMembers

        validateChallengeData(finalStartDate, finalEndDate, finalMaxMembers)

        // Use the entity's update method for clean updates
        val updatedChallenge = existingChallenge.updateWith(
            name = name,
            description = description,
            category = category,
            difficulty = difficulty,
            duration = duration,
            startDate = startDate,
            endDate = endDate,
            maxMembers = maxMembers,
            coverImageUrl = coverImageUrl,
            reward = reward,
            tags = tags
        )

        // Save using standard JPA repository - dirty checking will handle the update
        return challengeRepository.save(updatedChallenge)
    }

    @Transactional
    fun joinChallenge(user: User, challengeId: Long, joinReason: String? = null): Challenge {
        val challenge = findById(challengeId)

        if (challenge.status != ChallengeStatus.RECRUITING) {
            throw IllegalStateException("모집 중이 아닌 챌린지에는 참여할 수 없습니다.")
        }

        if (participationRepository.existsByUserAndChallenge(user, challenge)) {
            throw IllegalArgumentException("이미 참여한 챌린지입니다.")
        }

        val currentMembers = participationRepository.countByChallengeAndStatus(challenge, ParticipationStatus.JOINED)
        if (currentMembers >= challenge.maxMembers) {
            throw IllegalStateException("모집 정원이 마감되었습니다.")
        }

        val participation = Participation(
            user = user,
            challenge = challenge,
            status = ParticipationStatus.JOINED,
            joinReason = joinReason
        )

        participationRepository.save(participation)
        return challenge
    }

    @Transactional
    fun leaveChallenge(user: User, challengeId: Long): Challenge {
        val challenge = findById(challengeId)

        if (challenge.leader == user) {
            throw IllegalStateException("그룹 리더는 그룹을 탈퇴할 수 없습니다.")
        }

        val participation = participationRepository.findByUserAndChallenge(user, challenge)
            ?: throw IllegalArgumentException("참여하지 않은 챌린지입니다.")

        if (!participation.isActive()) {
            throw IllegalArgumentException("이미 탈퇴한 챌린지입니다.")
        }

        participation.leave()
        participationRepository.save(participation)
        return challenge
    }

    fun getChallengeMembers(challengeId: Long): List<Participation> {
        return participationRepository.findActiveParticipantsByChallengeId(challengeId)
    }

    fun getCurrentMemberCount(challenge: Challenge): Long {
        return participationRepository.countByChallengeAndStatus(challenge, ParticipationStatus.JOINED)
    }

    fun isUserMember(user: User, challenge: Challenge): Boolean {
        return participationRepository.existsByUserAndChallengeAndStatus(user, challenge, ParticipationStatus.JOINED)
    }

    // Challenge Application Management

    @Transactional
    fun applyToChallenge(user: User, challengeId: Long, reason: String): ChallengeApplication {
        val challenge = findById(challengeId)

        // Validation checks
        if (challenge.status != ChallengeStatus.RECRUITING) {
            throw IllegalStateException("모집 중이 아닌 챌린지에는 신청할 수 없습니다.")
        }

        if (participationRepository.existsByUserAndChallenge(user, challenge)) {
            throw IllegalArgumentException("이미 참여한 챌린지입니다.")
        }

        if (challengeApplicationRepository.existsByUserAndChallenge(user, challenge)) {
            throw IllegalArgumentException("이미 신청한 챌린지입니다.")
        }

        val currentMembers = participationRepository.countByChallengeAndStatus(challenge, ParticipationStatus.JOINED)
        if (currentMembers >= challenge.maxMembers) {
            throw IllegalStateException("모집 정원이 마감되었습니다.")
        }

        val application = ChallengeApplication(
            user = user,
            challenge = challenge,
            reason = reason
        )

        val savedApplication = challengeApplicationRepository.save(application)

        // 그룹 리더에게 새로운 신청 알림 전송
        notificationService.createNewApplicationNotification(
            managerId = challenge.leader.id!!,
            applicantName = "",
            challengeId = challenge.id.toString(),
            challengeName = challenge.name,
            applicationId = savedApplication.id.toString()
        )

        return savedApplication
    }

    fun getChallengeApplications(challengeId: Long, currentUser: User, status: ApplicationStatus?, pageable: Pageable): Page<ChallengeApplication> {
        val challenge = findById(challengeId)

        // Only challenge leader can view applications
        if (challenge.leader.id != currentUser.id) {
            throw IllegalArgumentException("그룹 신청 목록을 볼 권한이 없습니다.")
        }

        return if (status != null) {
            challengeApplicationRepository.findByChallengeAndStatus(challenge, status, pageable)
        } else {
            challengeApplicationRepository.findByChallenge(challenge, pageable)
        }
    }

    @Transactional
    fun updateApplicationStatus(
        challengeId: Long,
        applicationId: Long,
        currentUser: User,
        newStatus: ApplicationStatus,
        rejectionReason: String?
    ): ChallengeApplication {
        val challenge = findById(challengeId)

        // Only challenge leader can update application status
        if (challenge.leader.id != currentUser.id) {
            throw IllegalArgumentException("신청 상태를 변경할 권한이 없습니다.")
        }

        val application = challengeApplicationRepository.findById(applicationId)
            .orElseThrow { NoSuchElementException("신청을 찾을 수 없습니다: $applicationId") }

        if (application.challenge.id != challengeId) {
            throw IllegalArgumentException("해당 그룹의 신청이 아닙니다.")
        }

        if (application.isProcessed()) {
            throw IllegalStateException("이미 처리된 신청입니다.")
        }

        // Apply status change
        when (newStatus) {
            ApplicationStatus.APPROVED -> {
                application.approve()
                // Auto-join user to challenge
                autoJoinUserToChallenge(application.user, challenge)
                // Send notification to applicant
                notificationService.createApplicationApprovedNotification(
                    userId = application.user.id!!,
                    applicationId = application.id.toString(),
                    challengeName = challenge.name,
                    challengeId = challenge.id.toString()
                )
            }
            ApplicationStatus.REJECTED -> {
                if (rejectionReason.isNullOrBlank()) {
                    throw IllegalArgumentException("반려 시 반려 사유는 필수입니다.")
                }
                application.reject(rejectionReason)
                // Send notification to applicant
                notificationService.createApplicationRejectedNotification(
                    userId = application.user.id!!,
                    applicationId = application.id.toString(),
                    challengeName = challenge.name,
                    reason = rejectionReason
                )
            }
            ApplicationStatus.PENDING -> {
                throw IllegalArgumentException("대기 상태로 변경할 수 없습니다.")
            }
        }

        return challengeApplicationRepository.save(application)
    }

    fun getUserApplications(user: User, status: ApplicationStatus?, pageable: Pageable): Page<ChallengeApplication> {
        return if (status != null) {
            challengeApplicationRepository.findByUserAndStatus(user, status, pageable)
        } else {
            challengeApplicationRepository.findByUser(user, pageable)
        }
    }

    fun findApplicationById(applicationId: Long): ChallengeApplication {
        return challengeApplicationRepository.findById(applicationId)
            .orElseThrow { NoSuchElementException("신청을 찾을 수 없습니다: $applicationId") }
    }

    @Transactional
    private fun autoJoinUserToChallenge(user: User, challenge: Challenge) {
        // Check if user is already a member (shouldn't happen, but safety check)
        if (participationRepository.existsByUserAndChallenge(user, challenge)) {
            return
        }

        // Check challenge capacity again
        val currentMembers = participationRepository.countByChallengeAndStatus(challenge, ParticipationStatus.JOINED)
        if (currentMembers >= challenge.maxMembers) {
            throw IllegalStateException("모집 정원이 마감되어 자동 가입할 수 없습니다.")
        }

        val participation = Participation(
            user = user,
            challenge = challenge,
            status = ParticipationStatus.JOINED,
            joinReason = "신청 승인으로 인한 자동 가입"
        )

        participationRepository.save(participation)
    }

    private fun validateChallengeData(startDate: LocalDate, endDate: LocalDate, maxMembers: Int) {
        if (startDate.isBefore(LocalDate.now())) {
            throw IllegalArgumentException("시작일은 현재 날짜 이후여야 합니다.")
        }

        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw IllegalArgumentException("종료일은 시작일보다 뒤여야 합니다.")
        }

        if (maxMembers < 2 || maxMembers > 1000) {
            throw IllegalArgumentException("최대 참여자 수는 2명 이상 1000명 이하여야 합니다.")
        }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }

    /**
     * 사용자가 특정 챌린지를 볼 수 있는지 확인
     * Private 챌린지의 경우: 리더이거나 참여자인 경우에만 볼 수 있음
     * Public 챌린지의 경우: 누구나 볼 수 있음
     */
    fun canUserViewChallenge(user: User?, challenge: Challenge): Boolean {
        // Public 챌린지는 누구나 볼 수 있음 (인증되지 않은 사용자 포함)
        if (!challenge.isPrivate) {
            return true
        }

        // Private 챌린지는 인증된 사용자만 접근 가능
        if (user == null) {
            return false
        }

        // 챌린지 리더는 항상 볼 수 있음
        if (challenge.leader.id == user.id) {
            return true
        }

        // 챌린지 참여자인지 확인
        return participationRepository.existsByUserAndChallengeAndStatus(user, challenge, ParticipationStatus.JOINED)
    }

    /**
     * 사용자가 볼 수 있는 챌린지들만 필터링
     */
    fun filterVisibleChallenges(user: User?, challenges: List<Challenge>): List<Challenge> {
        return challenges.filter { canUserViewChallenge(user, it) }
    }

    /**
     * 사용자별 맞춤형 챌린지 목록 조회 (public + 사용자가 볼 수 있는 private)
     */
    fun findVisibleChallenges(user: User?, pageable: Pageable): Page<Challenge> {
        return if (user == null) {
            // 인증되지 않은 사용자는 public 챌린지만 볼 수 있음
            challengeRepository.findByIsPrivate(false, pageable)
        } else {
            // 인증된 사용자는 모든 챌린지를 가져온 후 필터링
            // TODO: 성능 최적화를 위해 나중에 SQL 쿼리로 개선 가능
            val allChallenges = challengeRepository.findAll(pageable)
            val visibleChallenges = filterVisibleChallenges(user, allChallenges.content)

            // Page 객체 재생성
            org.springframework.data.domain.PageImpl(
                visibleChallenges,
                pageable,
                visibleChallenges.size.toLong()
            )
        }
    }

    /**
     * 특정 사용자의 프로필에서 보여줄 챌린지 목록
     * - 해당 사용자가 생성한 챌린지
     * - 해당 사용자가 참여한 챌린지
     * - 현재 조회하는 사용자가 볼 수 있는 챌린지만 필터링
     */
    fun findUserChallenges(targetUserId: Long, currentUser: User?, pageable: Pageable): Page<Challenge> {
        val targetUser = userService.findById(targetUserId)

        // 해당 사용자가 리더인 챌린지
        val leaderChallenges = challengeRepository.findByLeader(targetUser, org.springframework.data.domain.Pageable.unpaged()).content

        // 해당 사용자가 참여한 챌린지
        val participatedChallenges = participationRepository.findByUserAndStatus(targetUser, ParticipationStatus.JOINED)
            .map { it.challenge }

        // 두 목록을 합치고 중복 제거
        val allUserChallenges = (leaderChallenges + participatedChallenges).distinctBy { it.id }

        // 현재 사용자가 볼 수 있는 챌린지만 필터링
        val visibleChallenges = filterVisibleChallenges(currentUser, allUserChallenges)

        // 페이징 적용
        val startIndex = (pageable.pageNumber * pageable.pageSize).coerceAtMost(visibleChallenges.size)
        val endIndex = ((pageable.pageNumber + 1) * pageable.pageSize).coerceAtMost(visibleChallenges.size)
        val pagedChallenges = if (startIndex < visibleChallenges.size) {
            visibleChallenges.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        return org.springframework.data.domain.PageImpl(
            pagedChallenges,
            pageable,
            visibleChallenges.size.toLong()
        )
    }

    @Transactional
    fun joinChallengeByInviteCode(user: User, inviteCode: String, joinReason: String? = null): Challenge {
        val challenge = challengeRepository.findByInviteCode(inviteCode)
            ?: throw NoSuchElementException("초대 코드에 해당하는 챌린지를 찾을 수 없습니다: $inviteCode")

        if (!challenge.isPrivate) {
            throw IllegalArgumentException("공개 챌린지는 초대 코드로 참여할 수 없습니다.")
        }

        if (challenge.status != ChallengeStatus.RECRUITING) {
            throw IllegalStateException("모집 중이 아닌 챌린지에는 참여할 수 없습니다.")
        }

        if (participationRepository.existsByUserAndChallenge(user, challenge)) {
            throw IllegalArgumentException("이미 참여한 챌린지입니다.")
        }

        val currentMembers = participationRepository.countByChallengeAndStatus(challenge, ParticipationStatus.JOINED)
        if (currentMembers >= challenge.maxMembers) {
            throw IllegalStateException("모집 정원이 마감되었습니다.")
        }

        val participation = Participation(
            user = user,
            challenge = challenge,
            status = ParticipationStatus.JOINED,
            joinReason = joinReason
        )

        participationRepository.save(participation)
        return challenge
    }

    fun getChallengeByInviteCode(inviteCode: String): Challenge {
        return challengeRepository.findByInviteCode(inviteCode)
            ?: throw NoSuchElementException("유효하지 않은 초대 코드입니다: $inviteCode")
    }

    // New Methods for Statistics

    fun getMemberStats(challengeId: Long, currentUser: User): List<com.habitchallenge.presentation.dto.MemberStatsResponse> {
        val challenge = findById(challengeId)

        // Permission check
        if (!canUserViewChallenge(currentUser, challenge)) {
            throw IllegalArgumentException("챌린지 통계를 볼 권한이 없습니다.")
        }

        val members = getChallengeMembers(challengeId)

        return members.map { participation ->
            val user = participation.user
            val allLogs = challengeLogRepository.findByUserAndChallenge(user, challenge)
            val approvedLogs = allLogs.filter { it.isApproved() }

            val totalSubmissions = allLogs.size
            val approvedSubmissions = approvedLogs.size
            val achievementRate = if (totalSubmissions > 0) {
                (approvedSubmissions.toDouble() / totalSubmissions.toDouble()) * 100.0
            } else {
                0.0
            }

            // Calculate streak
            val streak = calculateStreak(user, challenge)

            // Last submission date
            val lastSubmissionDate = allLogs.maxByOrNull { it.createdAt }?.createdAt

            com.habitchallenge.presentation.dto.MemberStatsResponse(
                memberId = user.id.toString(),
                streak = streak,
                achievementRate = achievementRate,
                totalSubmissions = totalSubmissions,
                approvedSubmissions = approvedSubmissions,
                lastSubmissionDate = lastSubmissionDate
            )
        }
    }

    fun getParticipationStats(
        challengeId: Long,
        currentUser: User,
        userId: Long?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): List<com.habitchallenge.presentation.dto.ParticipationStatsResponse> {
        val challenge = findById(challengeId)

        // Permission check
        if (!canUserViewChallenge(currentUser, challenge)) {
            throw IllegalArgumentException("챌린지 참여율 통계를 볼 권한이 없습니다.")
        }

        val actualStartDate = startDate ?: challenge.startDate
        val actualEndDate = endDate ?: challenge.endDate.let { if (it.isAfter(LocalDate.now())) LocalDate.now() else it }

        val members = getChallengeMembers(challengeId)
        val totalUserCount = members.size

        // Get target user if userId filter is provided
        val targetUser = userId?.let { userService.findById(it) }

        val result = mutableListOf<com.habitchallenge.presentation.dto.ParticipationStatsResponse>()

        var currentDate = actualStartDate
        while (!currentDate.isAfter(actualEndDate)) {
            val dayStartDateTime = currentDate.atStartOfDay()
            val dayEndDateTime = currentDate.plusDays(1).atStartOfDay()

            // Count submissions for this day
            val dailySubmissions = challengeLogRepository.findByUserAndChallenge(currentUser, challenge)
                .count { log ->
                    val logDate = log.createdAt.toLocalDate()
                    logDate.isEqual(currentDate)
                }

            // Calculate participation rate for this day
            var participantsCount = 0
            var userParticipated: Boolean? = null

            for (participation in members) {
                val hasSubmission = challengeLogRepository.existsByUserAndChallengeAndCreatedAtBetween(
                    participation.user, challenge, dayStartDateTime, dayEndDateTime
                )
                if (hasSubmission) {
                    participantsCount++
                }

                // Check if this is the target user
                if (targetUser != null && participation.user.id == targetUser.id) {
                    userParticipated = hasSubmission
                }
            }

            val participationRate = if (totalUserCount > 0) {
                (participantsCount.toDouble() / totalUserCount.toDouble()) * 100.0
            } else {
                0.0
            }

            result.add(
                com.habitchallenge.presentation.dto.ParticipationStatsResponse(
                    date = currentDate.toString(),
                    participated = userParticipated,
                    participationRate = participationRate,
                    submissions = dailySubmissions,
                    userCount = totalUserCount
                )
            )

            currentDate = currentDate.plusDays(1)
        }

        return result
    }

    private fun calculateStreak(user: User, challenge: Challenge): Int {
        val logs = challengeLogRepository.findByUserAndChallenge(user, challenge)
            .filter { it.isApproved() }
            .sortedBy { it.createdAt }

        if (logs.isEmpty()) return 0

        var streak = 0
        var lastDate: LocalDate? = null

        for (log in logs) {
            val logDate = log.createdAt.toLocalDate()

            if (lastDate == null) {
                streak = 1
                lastDate = logDate
            } else {
                if (logDate.isEqual(lastDate.plusDays(1))) {
                    streak++
                    lastDate = logDate
                } else if (!logDate.isEqual(lastDate)) {
                    streak = 1
                    lastDate = logDate
                }
            }
        }

        return streak
    }
}
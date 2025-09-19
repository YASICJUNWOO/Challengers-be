package com.habitchallenge.presentation.controller

import com.habitchallenge.application.service.ChallengeService
import com.habitchallenge.application.service.UserService
import com.habitchallenge.domain.challenge.ChallengeCategory
import com.habitchallenge.domain.challenge.ChallengeDifficulty
import com.habitchallenge.domain.challenge.ChallengeStatus
import com.habitchallenge.domain.challenge.LeaderRole
import com.habitchallenge.domain.challengeapplication.ApplicationStatus
import com.habitchallenge.domain.user.User
import com.habitchallenge.presentation.dto.*
import java.time.LocalDate
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/challenges")
class ChallengeController(
    private val challengeService: ChallengeService,
    private val userService: UserService
) {

    @GetMapping
    fun getChallenges(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) search: String?,
        @AuthenticationPrincipal currentUser: User?
    ): ResponseEntity<Page<ChallengeResponse>> {
        val pageable = PageRequest.of(page, size)

        // 현재 사용자가 볼 수 있는 챌린지들만 조회
        val challenges = when {
            search != null -> {
                val statusEnum = status?.let { ChallengeStatus.valueOf(it.uppercase()) }
                val allChallenges = challengeService.searchChallenges(search, statusEnum, pageable)
                val filteredChallenges = challengeService.filterVisibleChallenges(currentUser, allChallenges.content)

                org.springframework.data.domain.PageImpl(
                    filteredChallenges,
                    pageable,
                    filteredChallenges.size.toLong()
                )
            }
            category != null && status != null -> {
                val allChallenges = challengeService.findByCategoryAndStatus(
                    ChallengeCategory.valueOf(category.uppercase()),
                    ChallengeStatus.valueOf(status.uppercase()),
                    pageable
                )
                val filteredChallenges = challengeService.filterVisibleChallenges(currentUser, allChallenges.content)

                org.springframework.data.domain.PageImpl(
                    filteredChallenges,
                    pageable,
                    filteredChallenges.size.toLong()
                )
            }
            category != null -> {
                val allChallenges = challengeService.findByCategory(ChallengeCategory.valueOf(category.uppercase()), pageable)
                val filteredChallenges = challengeService.filterVisibleChallenges(currentUser, allChallenges.content)

                org.springframework.data.domain.PageImpl(
                    filteredChallenges,
                    pageable,
                    filteredChallenges.size.toLong()
                )
            }
            status != null -> {
                val allChallenges = challengeService.findByStatus(ChallengeStatus.valueOf(status.uppercase()), pageable)
                val filteredChallenges = challengeService.filterVisibleChallenges(currentUser, allChallenges.content)

                org.springframework.data.domain.PageImpl(
                    filteredChallenges,
                    pageable,
                    filteredChallenges.size.toLong()
                )
            }
            else -> {
                // 일반 목록 조회는 새로운 메소드 사용
                challengeService.findVisibleChallenges(currentUser, pageable)
            }
        }

        val challengeResponses = challenges.map { challenge ->
            val currentMemberCount = challengeService.getCurrentMemberCount(challenge)
            val members = challengeService.getChallengeMembers(challenge.id!!)
                .map { UserResponse.from(it.user) }

            ChallengeResponse.from(challenge, currentMemberCount, members)
        }

        return ResponseEntity.ok(challengeResponses)
    }

    @GetMapping("/invite/{inviteCode}")
    fun getChallengeByInviteCode(
        @PathVariable inviteCode: String
    ): ResponseEntity<ChallengeResponse> {
        try {
            val challenge = challengeService.getChallengeByInviteCode(inviteCode)

            // Invite code allows public access to private challenges
            // No permission check needed - having the invite code is sufficient

            val currentMemberCount = challengeService.getCurrentMemberCount(challenge)
            val members = challengeService.getChallengeMembers(challenge.id!!)
                .map { UserResponse.from(it.user) }

            val response = ChallengeResponse.from(challenge, currentMemberCount, members)
            return ResponseEntity.ok(response)
        } catch (e: NoSuchElementException) {
            return ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{id}")
    fun getChallengeById(
        @PathVariable id: Long,
        @AuthenticationPrincipal currentUser: User?
    ): ResponseEntity<ChallengeResponse> {
        val challenge = challengeService.findById(id)

        // 권한 체크: 사용자가 이 챌린지를 볼 수 있는지 확인
        if (!challengeService.canUserViewChallenge(currentUser, challenge)) {
            return ResponseEntity.notFound().build()
        }

        val currentMemberCount = challengeService.getCurrentMemberCount(challenge)
        val members = challengeService.getChallengeMembers(id)
            .map { UserResponse.from(it.user) }

        val response = ChallengeResponse.from(challenge, currentMemberCount, members)
        return ResponseEntity.ok(response)
    }

    @PostMapping
    fun createChallenge(
        @Valid @RequestBody request: CreateChallengeRequest,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ChallengeResponse> {
        val challenge = challengeService.createChallenge(
            name = request.name,
            description = request.description,
            category = ChallengeCategory.valueOf(request.category.uppercase()),
            difficulty = ChallengeDifficulty.valueOf(request.difficulty.uppercase()),
            duration = request.duration,
            startDate = java.time.LocalDate.parse(request.startDate),
            endDate = java.time.LocalDate.parse(request.endDate),
            maxMembers = request.maxMembers,
            leader = user,
            coverImageUrl = request.coverImage,
            reward = request.reward,
            tags = request.tags.toSet(),
            isPrivate = request.isPrivate,
            leaderRole = LeaderRole.valueOf(request.leaderRole.uppercase())
        )

        // Get current member count and members list
        val currentMemberCount = challengeService.getCurrentMemberCount(challenge)
        val members = challengeService.getChallengeMembers(challenge.id!!)
            .map { UserResponse.from(it.user) }

        val response = ChallengeResponse.from(
            challenge,
            currentMemberCount = currentMemberCount,
            members = members
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{id}/join")
    fun joinChallenge(
        @PathVariable id: Long,
        @Valid @RequestBody request: JoinChallengeRequest,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ChallengeResponse> {
        val challenge = challengeService.joinChallenge(user, id, request.joinReason)
        val currentMemberCount = challengeService.getCurrentMemberCount(challenge)
        val members = challengeService.getChallengeMembers(id)
            .map { UserResponse.from(it.user) }

        val response = ChallengeResponse.from(challenge, currentMemberCount, members)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}/leave")
    fun leaveChallenge(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ChallengeResponse> {
        val challenge = challengeService.leaveChallenge(user, id)
        val currentMemberCount = challengeService.getCurrentMemberCount(challenge)
        val members = challengeService.getChallengeMembers(id)
            .map { UserResponse.from(it.user) }

        val response = ChallengeResponse.from(challenge, currentMemberCount, members)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{id}/join")
    fun joinChallengeById(
        @PathVariable id: Long,
        @Valid @RequestBody request: JoinChallengeByUserIdRequest
    ): ResponseEntity<ChallengeResponse> {
        val user = userService.findById(request.userId.toLong())
        val challenge = challengeService.joinChallenge(user, id, request.joinReason)
        val currentMemberCount = challengeService.getCurrentMemberCount(challenge)
        val members = challengeService.getChallengeMembers(id)
            .map { UserResponse.from(it.user) }

        val response = ChallengeResponse.from(challenge, currentMemberCount, members)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/join-by-code")
    fun joinChallengeByInviteCode(
        @Valid @RequestBody request: JoinChallengeByInviteCodeRequest,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ChallengeResponse> {
        val challenge = challengeService.joinChallengeByInviteCode(user, request.inviteCode, request.joinReason)
        val currentMemberCount = challengeService.getCurrentMemberCount(challenge)
        val members = challengeService.getChallengeMembers(challenge.id!!)
            .map { UserResponse.from(it.user) }

        val response = ChallengeResponse.from(challenge, currentMemberCount, members)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    fun updateChallenge(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateChallengeRequest,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ChallengeResponse> {
        val updatedChallenge = challengeService.updateChallenge(
            challengeId = id,
            currentUser = user,
            name = request.name,
            description = request.description,
            category = request.category?.let { ChallengeCategory.valueOf(it.uppercase()) },
            difficulty = request.difficulty?.let { ChallengeDifficulty.valueOf(it.uppercase()) },
            duration = request.duration,
            startDate = request.startDate?.let { java.time.LocalDate.parse(it) },
            endDate = request.endDate?.let { java.time.LocalDate.parse(it) },
            maxMembers = request.maxMembers,
            coverImageUrl = request.coverImage,
            reward = request.reward,
            tags = request.tags?.toSet()
        )

        val currentMemberCount = challengeService.getCurrentMemberCount(updatedChallenge)
        val members = challengeService.getChallengeMembers(id)
            .map { UserResponse.from(it.user) }

        val response = ChallengeResponse.from(updatedChallenge, currentMemberCount, members)
        return ResponseEntity.ok(response)
    }

    // Challenge Application Management Endpoints

    @PostMapping("/{id}/apply")
    fun applyToChallenge(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateApplicationRequest,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ChallengeApplicationResponse> {
        val application = challengeService.applyToChallenge(user, id, request.reason)
        val response = ChallengeApplicationResponse.from(application)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{id}/applications")
    fun getChallengeApplications(
        @PathVariable id: Long,
        @RequestParam(required = false) status: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<Page<ChallengeApplicationResponse>> {
        val pageable = PageRequest.of(page, size)
        val statusEnum = status?.let { ApplicationStatus.valueOf(it.uppercase()) }

        val applications = challengeService.getChallengeApplications(id, user, statusEnum, pageable)
        val response = applications.map { ChallengeApplicationResponse.from(it) }

        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}/applications/{applicationId}/status")
    fun updateApplicationStatus(
        @PathVariable id: Long,
        @PathVariable applicationId: Long,
        @Valid @RequestBody request: UpdateApplicationStatusRequest,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ChallengeApplicationResponse> {
        request.validate()

        val application = challengeService.updateApplicationStatus(
            challengeId = id,
            applicationId = applicationId,
            currentUser = user,
            newStatus = request.toApplicationStatus(),
            rejectionReason = request.rejectionReason
        )

        val response = ChallengeApplicationResponse.from(application)
        return ResponseEntity.ok(response)
    }

    // Statistics Endpoints

    @GetMapping("/{id}/members/stats")
    fun getMemberStats(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<List<MemberStatsResponse>> {
        val stats = challengeService.getMemberStats(id, user)
        return ResponseEntity.ok(stats)
    }

    @GetMapping("/{id}/participation")
    fun getParticipationStats(
        @PathVariable id: Long,
        @RequestParam(required = false) userId: Long?,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<List<ParticipationStatsResponse>> {
        val parsedStartDate = startDate?.let { LocalDate.parse(it) }
        val parsedEndDate = endDate?.let { LocalDate.parse(it) }

        val stats = challengeService.getParticipationStats(id, user, userId, parsedStartDate, parsedEndDate)
        return ResponseEntity.ok(stats)
    }
}
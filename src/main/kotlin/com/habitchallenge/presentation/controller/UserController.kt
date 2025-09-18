package com.habitchallenge.presentation.controller

import com.habitchallenge.application.service.ChallengeService
import com.habitchallenge.application.service.UserService
import com.habitchallenge.domain.challengeapplication.ApplicationStatus
import com.habitchallenge.domain.user.User
import com.habitchallenge.domain.user.UserRole
import com.habitchallenge.presentation.dto.ChallengeApplicationResponse
import com.habitchallenge.presentation.dto.ChallengeResponse
import com.habitchallenge.presentation.dto.UserResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val challengeService: ChallengeService
) {

    @GetMapping
    fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) role: String?
    ): ResponseEntity<Page<UserResponse>> {
        val pageable = PageRequest.of(page, size)

        val users = if (role != null) {
            userService.findByRole(UserRole.valueOf(role.uppercase()), pageable)
        } else {
            userService.findAll(pageable)
        }

        val userResponses = users.map { UserResponse.from(it) }
        return ResponseEntity.ok(userResponses)
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserResponse> {
        val user = userService.findById(id)
        return ResponseEntity.ok(UserResponse.from(user))
    }

    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal currentUser: User): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(UserResponse.from(currentUser))
    }

    @GetMapping("/me/applications")
    fun getCurrentUserApplications(
        @RequestParam(required = false) status: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<Page<ChallengeApplicationResponse>> {
        val pageable = PageRequest.of(page, size)
        val statusEnum = status?.let { ApplicationStatus.valueOf(it.uppercase()) }

        val applications = challengeService.getUserApplications(currentUser, statusEnum, pageable)
        val response = applications.map { ChallengeApplicationResponse.from(it) }

        return ResponseEntity.ok(response)
    }

    @GetMapping("/me/challenges")
    fun getCurrentUserChallenges(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<Page<ChallengeResponse>> {
        val pageable = PageRequest.of(page, size)

        val challenges = challengeService.findUserChallenges(currentUser.id!!, currentUser, pageable)
        val challengeResponses = challenges.map { challenge ->
            val currentMemberCount = challengeService.getCurrentMemberCount(challenge)
            val members = challengeService.getChallengeMembers(challenge.id!!)
                .map { UserResponse.from(it.user) }

            ChallengeResponse.from(challenge, currentMemberCount, members)
        }

        return ResponseEntity.ok(challengeResponses)
    }

    @GetMapping("/{id}/challenges")
    fun getUserChallenges(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal currentUser: User?
    ): ResponseEntity<Page<ChallengeResponse>> {
        val pageable = PageRequest.of(page, size)

        val challenges = challengeService.findUserChallenges(id, currentUser, pageable)
        val challengeResponses = challenges.map { challenge ->
            val currentMemberCount = challengeService.getCurrentMemberCount(challenge)
            val members = challengeService.getChallengeMembers(challenge.id!!)
                .map { UserResponse.from(it.user) }

            ChallengeResponse.from(challenge, currentMemberCount, members)
        }

        return ResponseEntity.ok(challengeResponses)
    }
}
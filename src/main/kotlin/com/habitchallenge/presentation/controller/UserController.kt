package com.habitchallenge.presentation.controller

import com.habitchallenge.application.service.ChallengeService
import com.habitchallenge.application.service.UserService
import com.habitchallenge.domain.challengeapplication.ApplicationStatus
import com.habitchallenge.domain.user.User
import com.habitchallenge.domain.user.UserRole
import com.habitchallenge.presentation.dto.*
import jakarta.validation.Valid
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

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateUserRequest,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<*> {
        return try {
            val updatedUser = userService.updateUser(id, request.nickname, request.email, currentUser.id!!)
            val response = UserResponse.from(updatedUser)
            ResponseEntity.ok(response)
        } catch (e: SecurityException) {
            ResponseEntity.status(403).body(mapOf("error" to "접근 권한이 없습니다.", "message" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(mapOf("error" to "잘못된 요청", "message" to e.message))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(mapOf("error" to "사용자를 찾을 수 없습니다.", "message" to e.message))
        }
    }

    @PutMapping("/{id}/password")
    fun changePassword(
        @PathVariable id: Long,
        @Valid @RequestBody request: ChangePasswordRequest,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<*> {
        return try {
            userService.changePassword(id, request.currentPassword, request.newPassword, currentUser.id!!)
            ResponseEntity.ok(mapOf("message" to "비밀번호가 성공적으로 변경되었습니다.", "success" to true))
        } catch (e: SecurityException) {
            ResponseEntity.status(403).body(mapOf("error" to "접근 권한이 없습니다.", "message" to e.message))
        } catch (e: IllegalArgumentException) {
            if (e.message?.contains("소셜 로그인") == true) {
                ResponseEntity.status(403).body(mapOf("error" to "소셜 로그인 계정은 비밀번호를 변경할 수 없습니다.", "message" to e.message))
            } else {
                ResponseEntity.status(400).body(mapOf("error" to "잘못된 요청", "message" to e.message))
            }
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(mapOf("error" to "사용자를 찾을 수 없습니다.", "message" to e.message))
        }
    }
}
package com.habitchallenge.presentation.controller

import com.habitchallenge.application.service.ChallengeLogService
import com.habitchallenge.domain.challengelog.LogStatus
import com.habitchallenge.domain.user.User
import com.habitchallenge.presentation.dto.*
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/challengeLogs")
class ChallengeLogController(private val challengeLogService: ChallengeLogService) {

    @GetMapping
    fun getChallengeLogs(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) challengeId: String?,
        @RequestParam(required = false) userId: String?,
        @RequestParam(required = false) status: String?
    ): ResponseEntity<Page<ChallengeLogResponse>> {
        val pageable = PageRequest.of(page, size)

        val logs = challengeLogService.findWithFilters(
            challengeId = challengeId?.toLongOrNull(),
            userId = userId?.toLongOrNull(),
            status = status?.let { LogStatus.valueOf(it.uppercase()) },
            pageable = pageable
        )

        val logResponses = logs.map { ChallengeLogResponse.from(it) }
        return ResponseEntity.ok(logResponses)
    }

    @GetMapping("/{id}")
    fun getChallengeLogById(@PathVariable id: Long): ResponseEntity<ChallengeLogResponse> {
        val log = challengeLogService.findById(id)
        return ResponseEntity.ok(ChallengeLogResponse.from(log))
    }

    @PostMapping
    fun createChallengeLog(
        @Valid @RequestBody request: CreateChallengeLogRequest,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ChallengeLogResponse> {
        val log = challengeLogService.createChallengeLog(
            user = user,
            challengeId = request.challengeId.toLong(),
            content = request.content,
            imageUrl = request.imageUrl
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ChallengeLogResponse.from(log))
    }

    @PutMapping("/{id}/approve")
    fun approveChallengeLog(
        @PathVariable id: Long,
        @Valid @RequestBody request: ReviewChallengeLogRequest,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ChallengeLogResponse> {
        val log = challengeLogService.approveChallengeLog(id, user, request.comment)
        return ResponseEntity.ok(ChallengeLogResponse.from(log))
    }

    @PutMapping("/{id}/reject")
    fun rejectChallengeLog(
        @PathVariable id: Long,
        @Valid @RequestBody request: ReviewChallengeLogRequest,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ChallengeLogResponse> {
        if (request.comment.isNullOrBlank()) {
            throw IllegalArgumentException("반려 시 코멘트는 필수입니다.")
        }

        val log = challengeLogService.rejectChallengeLog(id, user, request.comment)
        return ResponseEntity.ok(ChallengeLogResponse.from(log))
    }
}
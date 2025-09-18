package com.habitchallenge.application.service

import com.habitchallenge.domain.challengelog.ChallengeLog
import com.habitchallenge.domain.challengelog.ChallengeLogRepository
import com.habitchallenge.domain.challengelog.LogStatus
import com.habitchallenge.domain.participation.ParticipationRepository
import com.habitchallenge.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ChallengeLogService(
    private val challengeLogRepository: ChallengeLogRepository,
    private val participationRepository: ParticipationRepository,
    private val challengeService: ChallengeService,
    private val notificationService: NotificationService
) {

    @Transactional
    fun createChallengeLog(
        user: User,
        challengeId: Long,
        content: String,
        imageUrl: String? = null
    ): ChallengeLog {
        val challenge = challengeService.findById(challengeId)

        // 사용자가 해당 챌린지의 멤버인지 확인
        if (!challengeService.isUserMember(user, challenge)) {
            throw IllegalStateException("해당 챌린지의 멤버만 인증을 업로드할 수 있습니다.")
        }

        val challengeLog = ChallengeLog(
            user = user,
            challenge = challenge,
            content = content,
            imageUrl = imageUrl,
            status = LogStatus.PENDING
        )

        val savedLog = challengeLogRepository.save(challengeLog)

        // 챌린지 리더에게 새로운 인증 업로드 알림 전송
        notificationService.createNewChallengeLogNotification(
            managerId = challenge.leader.id!!,
            participantName = user.name,
            challengeId = challenge.id.toString(),
            challengeName = challenge.name,
            challengeLogId = savedLog.id.toString()
        )

        return savedLog
    }

    @Transactional
    fun approveChallengeLog(logId: Long, approver: User, comment: String? = null): ChallengeLog {
        val log = findById(logId)

        // 챌린지 리더만 승인 가능
        if (log.challenge.leader != approver) {
            throw IllegalStateException("챌린지 리더만 인증을 승인할 수 있습니다.")
        }

        if (!log.isPending()) {
            throw IllegalStateException("대기 중인 인증만 승인할 수 있습니다.")
        }

        log.approve(comment)
        val savedLog = challengeLogRepository.save(log)

        // 참여자에게 인증 승인 알림 전송
        notificationService.createChallengeApprovedNotification(
            userId = log.user.id!!,
            challengeLogId = log.id.toString(),
            challengeName = log.challenge.name
        )

        return savedLog
    }

    @Transactional
    fun rejectChallengeLog(logId: Long, rejector: User, comment: String): ChallengeLog {
        val log = findById(logId)

        // 챌린지 리더만 반려 가능
        if (log.challenge.leader != rejector) {
            throw IllegalStateException("챌린지 리더만 인증을 반려할 수 있습니다.")
        }

        if (!log.isPending()) {
            throw IllegalStateException("대기 중인 인증만 반려할 수 있습니다.")
        }

        log.reject(comment)
        val savedLog = challengeLogRepository.save(log)

        // 참여자에게 인증 반려 알림 전송
        notificationService.createChallengeRejectedNotification(
            userId = log.user.id!!,
            challengeLogId = log.id.toString(),
            challengeName = log.challenge.name,
            reason = comment
        )

        return savedLog
    }

    fun findById(id: Long): ChallengeLog {
        return challengeLogRepository.findById(id)
            .orElseThrow { NoSuchElementException("챌린지 로그를 찾을 수 없습니다: $id") }
    }

    fun findWithFilters(
        challengeId: Long? = null,
        userId: Long? = null,
        status: LogStatus? = null,
        pageable: Pageable
    ): Page<ChallengeLog> {
        return challengeLogRepository.findWithFilters(challengeId, userId, status, pageable)
    }

    fun findApprovedLogsByChallenge(challengeId: Long, pageable: Pageable): Page<ChallengeLog> {
        val challenge = challengeService.findById(challengeId)
        return challengeLogRepository.findApprovedLogsByChallenge(challenge, pageable)
    }

    fun findApprovedLogsByUser(user: User, pageable: Pageable): Page<ChallengeLog> {
        return challengeLogRepository.findApprovedLogsByUser(user, pageable)
    }

    fun findPendingLogsByChallenge(challengeId: Long): List<ChallengeLog> {
        val challenge = challengeService.findById(challengeId)
        return challengeLogRepository.findPendingLogsByChallenge(challenge)
    }

    fun findByStatus(status: LogStatus, pageable: Pageable): Page<ChallengeLog> {
        return challengeLogRepository.findByStatus(status, pageable)
    }
}
package com.habitchallenge.application.service

import com.habitchallenge.domain.challengelog.ChallengeLogRepository
import com.habitchallenge.domain.challengelog.LogStatus
import com.habitchallenge.domain.challenge.ChallengeRepository
import com.habitchallenge.domain.challenge.ChallengeStatus
import com.habitchallenge.domain.challenge.start
import com.habitchallenge.domain.challenge.complete
import com.habitchallenge.domain.participation.ParticipationRepository
import com.habitchallenge.domain.participation.ParticipationStatus
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class NotificationSchedulerService(
    private val challengeRepository: ChallengeRepository,
    private val participationRepository: ParticipationRepository,
    private val challengeLogRepository: ChallengeLogRepository,
    private val notificationService: NotificationService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 오늘 인증을 제출하지 않은 참여자들에게 리마인드 알림 전송
     * 매일 저녁 8시에 실행
     */
    @Scheduled(cron = "0 0 20 * * *")
    @Transactional
    fun sendDailyReminders() {
        logger.info("Starting daily reminder notifications")

        val today = LocalDate.now()
        var reminderCount = 0

        // 현재 진행 중인 모든 챌린지 조회
        val activeChallenges = challengeRepository.findByStatus(ChallengeStatus.ACTIVE)

        for (challenge in activeChallenges) {
            // 챌린지의 모든 활성 참여자 조회
            val activeParticipations = participationRepository.findByChallengeAndStatus(challenge, ParticipationStatus.JOINED)

            for (participation in activeParticipations) {
                // 오늘 해당 참여자가 인증을 제출했는지 확인
                val hasSubmittedToday = challengeLogRepository.existsByUserAndChallengeAndCreatedAtBetween(
                    user = participation.user,
                    challenge = challenge,
                    startDateTime = today.atStartOfDay(),
                    endDateTime = today.plusDays(1).atStartOfDay()
                )

                if (!hasSubmittedToday) {
                    // 오늘 인증을 제출하지 않은 참여자에게 리마인드 알림 전송
                    notificationService.createDailyReminderNotification(
                        userId = participation.user.id!!,
                        challengeId = challenge.id.toString(),
                        challengeName = challenge.name
                    )
                    reminderCount++
                }
            }
        }

        logger.info("Sent {} daily reminder notifications", reminderCount)
    }

    /**
     * 승인 대기 중인 인증이 있는 매니저들에게 요약 알림 전송
     * 매일 오전 9시에 실행
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    fun sendDailyApprovalSummaries() {
        logger.info("Starting daily approval summary notifications")

        var summaryCount = 0

        // 현재 진행 중인 모든 챌린지 조회
        val activeChallenges = challengeRepository.findByStatus(ChallengeStatus.ACTIVE)

        for (challenge in activeChallenges) {
            // 해당 챌린지의 승인 대기 중인 인증 개수 조회
            val pendingCount = challengeLogRepository.countByChallengeAndStatus(challenge, LogStatus.PENDING)

            if (pendingCount > 0) {
                // 승인 대기 중인 인증이 있는 경우 챌린지 리더에게 요약 알림 전송
                notificationService.createDailyApprovalSummaryNotification(
                    managerId = challenge.leader.id!!,
                    challengeId = challenge.id.toString(),
                    challengeName = challenge.name,
                    pendingCount = pendingCount.toInt()
                )
                summaryCount++
            }
        }

        logger.info("Sent {} daily approval summary notifications", summaryCount)
    }

    /**
     * 챌린지 시작 알림 전송
     * 매일 오전 10시에 실행하여 오늘 시작하는 챌린지를 확인
     */
    @Scheduled(cron = "0 0 10 * * *")
    @Transactional
    fun sendChallengeStartNotifications() {
        logger.info("Checking for challenges starting today")

        val today = LocalDate.now()
        var startNotificationCount = 0

        // 오늘 시작하는 챌린지들 조회
        val startingChallenges = challengeRepository.findByStartDate(today)

        for (challenge in startingChallenges) {
            // 챌린지 상태를 ACTIVE로 변경
            challenge.start()
            challengeRepository.save(challenge)

            // 모든 참여자들에게 시작 알림 전송
            val participants = participationRepository.findByChallengeAndStatus(challenge, ParticipationStatus.JOINED)

            for (participation in participants) {
                notificationService.createChallengeStartedNotification(
                    userId = participation.user.id!!,
                    challengeId = challenge.id.toString(),
                    challengeName = challenge.name
                )
                startNotificationCount++
            }

            // 챌린지 리더에게도 시작 알림 전송 (참여자가 아닌 경우)
            val leaderIsParticipant = participants.any { it.user.id == challenge.leader.id }
            if (!leaderIsParticipant) {
                notificationService.createChallengeStartedNotification(
                    userId = challenge.leader.id!!,
                    challengeId = challenge.id.toString(),
                    challengeName = challenge.name
                )
                startNotificationCount++
            }
        }

        logger.info("Sent {} challenge start notifications for {} challenges", startNotificationCount, startingChallenges.size)
    }

    /**
     * 챌린지 종료 알림 전송
     * 매일 오후 11시에 실행하여 오늘 종료하는 챌린지를 확인
     */
    @Scheduled(cron = "0 0 23 * * *")
    @Transactional
    fun sendChallengeEndNotifications() {
        logger.info("Checking for challenges ending today")

        val today = LocalDate.now()
        var endNotificationCount = 0

        // 오늘 종료하는 챌린지들 조회
        val endingChallenges = challengeRepository.findByEndDate(today)

        for (challenge in endingChallenges) {
            // 챌린지 상태를 COMPLETED로 변경
            challenge.complete()
            challengeRepository.save(challenge)

            // 모든 참여자들에게 종료 알림 전송
            val participants = participationRepository.findByChallengeAndStatus(challenge, ParticipationStatus.JOINED)

            for (participation in participants) {
                notificationService.createChallengeEndedNotification(
                    userId = participation.user.id!!,
                    challengeId = challenge.id.toString(),
                    challengeName = challenge.name
                )
                endNotificationCount++
            }

            // 챌린지 리더에게도 종료 알림 전송 (참여자가 아닌 경우)
            val leaderIsParticipant = participants.any { it.user.id == challenge.leader.id }
            if (!leaderIsParticipant) {
                notificationService.createChallengeEndedNotification(
                    userId = challenge.leader.id!!,
                    challengeId = challenge.id.toString(),
                    challengeName = challenge.name
                )
                endNotificationCount++
            }
        }

        logger.info("Sent {} challenge end notifications for {} challenges", endNotificationCount, endingChallenges.size)
    }
}
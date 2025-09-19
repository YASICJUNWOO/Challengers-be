package com.habitchallenge.application.service

import com.habitchallenge.domain.notification.Notification
import com.habitchallenge.domain.notification.NotificationRepository
import com.habitchallenge.domain.notification.NotificationType
import com.habitchallenge.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userService: UserService
) {

    @Transactional
    fun createNotification(
        userId: Long,
        type: NotificationType,
        title: String,
        message: String,
        relatedId: String? = null,
        actionUrl: String? = null
    ): Notification {
        val user = userService.findById(userId)

        val notification = Notification(
            user = user,
            type = type,
            title = title,
            message = message,
            relatedId = relatedId,
            actionUrl = actionUrl
        )

        return notificationRepository.save(notification)
    }

    fun findUserNotifications(user: User, pageable: Pageable): Page<Notification> {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable)
    }

    fun findUserNotificationsByReadStatus(user: User, isRead: Boolean, pageable: Pageable): Page<Notification> {
        return notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, isRead, pageable)
    }

    fun findUserNotificationsByType(user: User, type: NotificationType, pageable: Pageable): Page<Notification> {
        return notificationRepository.findByUserAndTypeOrderByCreatedAtDesc(user, type, pageable)
    }

    fun findUserNotificationsByReadStatusAndType(
        user: User,
        isRead: Boolean,
        type: NotificationType,
        pageable: Pageable
    ): Page<Notification> {
        return notificationRepository.findByUserAndIsReadAndTypeOrderByCreatedAtDesc(user, isRead, type, pageable)
    }

    fun findById(id: Long): Notification {
        return notificationRepository.findById(id)
            .orElseThrow { NoSuchElementException("알림을 찾을 수 없습니다: $id") }
    }

    @Transactional
    fun markAsRead(notificationId: Long, user: User): Notification {
        val notification = findById(notificationId)

        if (notification.user.id != user.id) {
            throw IllegalArgumentException("해당 알림에 대한 권한이 없습니다.")
        }

        notification.markAsRead()
        return notificationRepository.save(notification)
    }

    @Transactional
    fun markAllAsRead(user: User): Int {
        return notificationRepository.markAllAsReadByUser(user)
    }

    @Transactional
    fun deleteNotification(notificationId: Long, user: User) {
        if (!notificationRepository.existsByIdAndUser(notificationId, user)) {
            throw IllegalArgumentException("해당 알림에 대한 권한이 없거나 알림이 존재하지 않습니다.")
        }

        notificationRepository.deleteById(notificationId)
    }

    fun countUnreadNotifications(user: User): Long {
        return notificationRepository.countByUserAndIsRead(user, false)
    }

    // Helper methods for creating specific notification types
    @Transactional
    fun createChallengeApprovedNotification(userId: Long, challengeLogId: String, challengeName: String): Notification {
        return createNotification(
            userId = userId,
            type = NotificationType.CHALLENGE_APPROVED,
            title = "챌린지 인증 승인",
            message = "'${challengeName}' 챌린지의 인증이 승인되었습니다.",
            relatedId = challengeLogId,
            actionUrl = "/challenges/${challengeLogId}"
        )
    }

    @Transactional
    fun createChallengeRejectedNotification(userId: Long, challengeLogId: String, challengeName: String, reason: String): Notification {
        return createNotification(
            userId = userId,
            type = NotificationType.CHALLENGE_REJECTED,
            title = "챌린지 인증 반려",
            message = "'${challengeName}' 챌린지의 인증이 반려되었습니다. 사유: $reason",
            relatedId = challengeLogId,
            actionUrl = "/challenges/${challengeLogId}"
        )
    }

    @Transactional
    fun createChallengeJoinedNotification(userId: Long, challengeId: String, challengeName: String): Notification {
        return createNotification(
            userId = userId,
            type = NotificationType.GROUP_JOINED,
            title = "그룹 참여 완료",
            message = "'${challengeName}' 그룹에 성공적으로 참여했습니다.",
            relatedId = challengeId,
            actionUrl = "/challenges/${challengeId}"
        )
    }

    @Transactional
    fun createChallengeStartedNotification(userId: Long, challengeId: String, challengeName: String): Notification {
        return createNotification(
            userId = userId,
            type = NotificationType.GROUP_STARTED,
            title = "챌린지 시작",
            message = "'${challengeName}' 챌린지가 시작되었습니다! 첫 번째 인증을 해보세요.",
            relatedId = challengeId,
            actionUrl = "/challenges/${challengeId}"
        )
    }

    @Transactional
    fun createChallengeEndedNotification(userId: Long, challengeId: String, challengeName: String): Notification {
        return createNotification(
            userId = userId,
            type = NotificationType.GROUP_ENDED,
            title = "챌린지 종료",
            message = "'${challengeName}' 챌린지가 종료되었습니다. 결과를 확인해보세요.",
            relatedId = challengeId,
            actionUrl = "/challenges/${challengeId}/results"
        )
    }

    @Transactional
    fun createApplicationApprovedNotification(userId: Long, applicationId: String, challengeName: String, challengeId: String): Notification {
        return createNotification(
            userId = userId,
            type = NotificationType.APPLICATION_APPROVED,
            title = "그룹 참여 신청 승인",
            message = "'${challengeName}' 그룹 참여 신청이 승인되었습니다. 이제 챌린지에 참여할 수 있습니다!",
            relatedId = applicationId,
            actionUrl = "/challenges/${challengeId}"
        )
    }

    @Transactional
    fun createApplicationRejectedNotification(userId: Long, applicationId: String, challengeName: String, reason: String): Notification {
        return createNotification(
            userId = userId,
            type = NotificationType.APPLICATION_REJECTED,
            title = "그룹 참여 신청 반려",
            message = "'${challengeName}' 그룹 참여 신청이 반려되었습니다. 사유: $reason",
            relatedId = applicationId,
            actionUrl = "/applications"
        )
    }

    @Transactional
    fun createSystemNotification(userId: Long, title: String, message: String, actionUrl: String? = null): Notification {
        return createNotification(
            userId = userId,
            type = NotificationType.SYSTEM,
            title = title,
            message = message,
            actionUrl = actionUrl
        )
    }

    // New notification helper methods

    @Transactional
    fun createNewChallengeLogNotification(managerId: Long, participantName: String, challengeId: String, challengeName: String, challengeLogId: String): Notification {
        return createNotification(
            userId = managerId,
            type = NotificationType.NEW_CHALLENGE_LOG,
            title = "새로운 인증 업로드",
            message = "'${challengeName}' 그룹에서 ${participantName}님이 새로운 인증을 업로드했습니다.",
            relatedId = challengeLogId,
            actionUrl = "/manage/${challengeId}"
        )
    }

    @Transactional
    fun createNewApplicationNotification(managerId: Long, applicantName: String, challengeId: String, challengeName: String, applicationId: String): Notification {
        return createNotification(
            userId = managerId,
            type = NotificationType.NEW_APPLICATION,
            title = "새로운 참여 신청",
            message = "'${challengeName}' 그룹에 ${applicantName}님이 참여 신청했습니다.",
            relatedId = applicationId,
            actionUrl = "/challenges/${challengeId}/applications"
        )
    }

    @Transactional
    fun createDailyReminderNotification(userId: Long, challengeId: String, challengeName: String): Notification {
        return createNotification(
            userId = userId,
            type = NotificationType.DAILY_REMINDER,
            title = "인증 미제출 리마인드",
            message = "'${challengeName}' 챌린지의 오늘 인증을 아직 제출하지 않으셨습니다. 지금 인증해보세요!",
            relatedId = challengeId,
            actionUrl = "/challenges/${challengeId}/upload"
        )
    }

    @Transactional
    fun createDailyApprovalSummaryNotification(managerId: Long, challengeId: String, challengeName: String, pendingCount: Int): Notification {
        return createNotification(
            userId = managerId,
            type = NotificationType.DAILY_APPROVAL_SUMMARY,
            title = "승인 대기 인증 요약",
            message = "'${challengeName}' 그룹에 승인 대기 중인 인증이 ${pendingCount}개 있습니다. 확인해주세요.",
            relatedId = challengeId,
            actionUrl = "/manage/${challengeId}"
        )
    }
}
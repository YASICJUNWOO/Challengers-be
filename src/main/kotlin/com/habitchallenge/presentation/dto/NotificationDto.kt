package com.habitchallenge.presentation.dto

import com.habitchallenge.domain.notification.Notification
import com.habitchallenge.domain.notification.NotificationType
import jakarta.validation.constraints.*
import java.time.format.DateTimeFormatter

data class NotificationResponse(
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val read: Boolean,
    val createdAt: String,
    val userId: String,
    val relatedId: String?,
    val actionUrl: String?
) {
    companion object {
        fun from(notification: Notification): NotificationResponse {
            return NotificationResponse(
                id = notification.id.toString(),
                type = when (notification.type) {
                    NotificationType.CHALLENGE_APPROVED -> "challenge_approved"
                    NotificationType.CHALLENGE_REJECTED -> "challenge_rejected"
                    NotificationType.GROUP_JOINED -> "challenge_joined"
                    NotificationType.GROUP_STARTED -> "challenge_started"
                    NotificationType.GROUP_ENDED -> "challenge_ended"
                    NotificationType.APPLICATION_APPROVED -> "application_approved"
                    NotificationType.APPLICATION_REJECTED -> "application_rejected"
                    NotificationType.SYSTEM -> "system"
                    NotificationType.NEW_CHALLENGE_LOG -> "new_challenge_log"
                    NotificationType.NEW_APPLICATION -> "new_application"
                    NotificationType.DAILY_REMINDER -> "daily_reminder"
                    NotificationType.DAILY_APPROVAL_SUMMARY -> "daily_approval_summary"
                },
                title = notification.title,
                message = notification.message,
                read = notification.isRead,
                createdAt = notification.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                userId = notification.user.id.toString(),
                relatedId = notification.relatedId,
                actionUrl = notification.actionUrl
            )
        }
    }
}

data class CreateNotificationRequest(
    @field:NotBlank(message = "사용자 ID는 필수입니다.")
    val userId: String,

    @field:NotBlank(message = "알림 타입은 필수입니다.")
    @field:Pattern(
        regexp = "challenge_approved|challenge_rejected|group_joined|group_started|group_ended|application_approved|application_rejected|system",
        message = "올바른 알림 타입이 아닙니다."
    )
    val type: String,

    @field:NotBlank(message = "제목은 필수입니다.")
    @field:Size(min = 1, max = 100, message = "제목은 1자 이상 100자 이하여야 합니다.")
    val title: String,

    @field:NotBlank(message = "메시지는 필수입니다.")
    @field:Size(min = 1, max = 500, message = "메시지는 1자 이상 500자 이하여야 합니다.")
    val message: String,

    val relatedId: String? = null,

    @field:Size(max = 500, message = "액션 URL은 500자 이하여야 합니다.")
    val actionUrl: String? = null
) {
    fun toNotificationType(): NotificationType {
        return when (type) {
            "challenge_approved" -> NotificationType.CHALLENGE_APPROVED
            "challenge_rejected" -> NotificationType.CHALLENGE_REJECTED
            "group_joined" -> NotificationType.GROUP_JOINED
            "group_started" -> NotificationType.GROUP_STARTED
            "group_ended" -> NotificationType.GROUP_ENDED
            "application_approved" -> NotificationType.APPLICATION_APPROVED
            "application_rejected" -> NotificationType.APPLICATION_REJECTED
            "system" -> NotificationType.SYSTEM
            else -> throw IllegalArgumentException("Unknown notification type: $type")
        }
    }
}

data class MarkAllReadResponse(
    val updatedCount: Int
)
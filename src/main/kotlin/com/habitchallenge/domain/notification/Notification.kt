package com.habitchallenge.domain.notification

import com.habitchallenge.domain.common.BaseEntity
import com.habitchallenge.domain.user.User
import jakarta.persistence.*

@Entity
@Table(name = "notifications", indexes = [
    Index(name = "idx_notification_user_id", columnList = "user_id"),
    Index(name = "idx_notification_created_at", columnList = "created_at"),
    Index(name = "idx_notification_read_status", columnList = "is_read")
])
class Notification(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: NotificationType,

    @Column(nullable = false, length = 100)
    val title: String,

    @Column(nullable = false, length = 500)
    val message: String,

    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,

    @Column(name = "related_id")
    val relatedId: String? = null,

    @Column(name = "action_url", length = 500)
    val actionUrl: String? = null
) : BaseEntity() {

    fun markAsRead() {
        this.isRead = true
    }
}

enum class NotificationType {
    CHALLENGE_APPROVED,       // Maps to 'challenge_approved' in frontend
    CHALLENGE_REJECTED,       // Maps to 'challenge_rejected' in frontend
    GROUP_JOINED,             // Maps to 'group_joined' in frontend
    GROUP_STARTED,            // Maps to 'group_started' in frontend
    GROUP_ENDED,              // Maps to 'group_ended' in frontend
    APPLICATION_APPROVED,     // Maps to 'application_approved' in frontend
    APPLICATION_REJECTED,     // Maps to 'application_rejected' in frontend
    NEW_CHALLENGE_LOG,        // Maps to 'new_challenge_log' in frontend - 새로운 인증 업로드 발생 (매니저 대상)
    NEW_APPLICATION,          // Maps to 'new_application' in frontend - 참여 신청 발생 (매니저 대상)
    DAILY_REMINDER,           // Maps to 'daily_reminder' in frontend - 오늘 인증 미제출 리마인드 (참여자 대상)
    DAILY_APPROVAL_SUMMARY,   // Maps to 'daily_approval_summary' in frontend - 승인 대기 인증 요약 (매니저 대상)
    SYSTEM                    // Maps to 'system' in frontend
}
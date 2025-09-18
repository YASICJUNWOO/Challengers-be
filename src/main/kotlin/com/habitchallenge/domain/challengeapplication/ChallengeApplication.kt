package com.habitchallenge.domain.challengeapplication

import com.habitchallenge.domain.common.BaseEntity
import com.habitchallenge.domain.challenge.Challenge
import com.habitchallenge.domain.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "challenge_applications",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "challenge_id"])
    ]
)
class ChallengeApplication(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    val challenge: Challenge,

    @Column(nullable = false, length = 500)
    val reason: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ApplicationStatus = ApplicationStatus.PENDING,

    @Column(name = "reviewed_at")
    var reviewedAt: LocalDateTime? = null,

    @Column(name = "rejection_reason", length = 500)
    var rejectionReason: String? = null
) : BaseEntity() {

    fun approve() {
        status = ApplicationStatus.APPROVED
        reviewedAt = LocalDateTime.now()
        rejectionReason = null
    }

    fun reject(reason: String) {
        status = ApplicationStatus.REJECTED
        reviewedAt = LocalDateTime.now()
        rejectionReason = reason
    }

    fun isPending(): Boolean = status == ApplicationStatus.PENDING
    fun isApproved(): Boolean = status == ApplicationStatus.APPROVED
    fun isRejected(): Boolean = status == ApplicationStatus.REJECTED
    fun isProcessed(): Boolean = status != ApplicationStatus.PENDING
}

enum class ApplicationStatus {
    PENDING,    // Waiting for review
    APPROVED,   // Approved by challenge leader
    REJECTED    // Rejected by challenge leader
}
package com.habitchallenge.domain.challengelog

import com.habitchallenge.domain.common.BaseEntity
import com.habitchallenge.domain.challenge.Challenge
import com.habitchallenge.domain.user.User
import jakarta.persistence.*

@Entity
@Table(name = "challenge_logs")
class ChallengeLog(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    val challenge: Challenge,

    @Column(nullable = false, length = 2000)
    val content: String,

    @Lob // 이후 이미지 저장 방식에 따라 변경 가능
    @Column(name = "image_url")
    val imageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: LogStatus = LogStatus.PENDING,

    @Column(name = "rejection_comment", length = 500)
    var rejectionComment: String? = null
) : BaseEntity() {

    fun approve(comment: String? = null) {
        this.status = LogStatus.APPROVED
        this.rejectionComment = comment
    }

    fun reject(comment: String) {
        this.status = LogStatus.REJECTED
        this.rejectionComment = comment
    }

    fun isPending(): Boolean = status == LogStatus.PENDING
    fun isApproved(): Boolean = status == LogStatus.APPROVED
    fun isRejected(): Boolean = status == LogStatus.REJECTED
}

enum class LogStatus {
    PENDING,    // Maps to 'pending'
    APPROVED,   // Maps to 'approved'
    REJECTED    // Maps to 'rejected'
}
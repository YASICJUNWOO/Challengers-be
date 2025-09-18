package com.habitchallenge.domain.participation

import com.habitchallenge.domain.common.BaseEntity
import com.habitchallenge.domain.challenge.Challenge
import com.habitchallenge.domain.user.User
import jakarta.persistence.*

@Entity
@Table(
    name = "participations",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "challenge_id"])
    ]
)
class Participation(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    val challenge: Challenge,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ParticipationStatus = ParticipationStatus.JOINED,

    @Column(name = "join_reason", length = 500)
    val joinReason: String? = null
) : BaseEntity() {

    fun leave() {
        status = ParticipationStatus.LEFT
    }

    fun isActive(): Boolean = status == ParticipationStatus.JOINED
}

enum class ParticipationStatus {
    JOINED,     // Active participation
    LEFT        // Left the challenge
}
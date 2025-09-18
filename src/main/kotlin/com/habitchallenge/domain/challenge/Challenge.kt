package com.habitchallenge.domain.challenge

import com.habitchallenge.domain.common.BaseEntity
import com.habitchallenge.domain.user.User
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "challenges")
class Challenge(
    @Column(nullable = false)
    val name: String,

    @Column(length = 1000, nullable = false)
    val description: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val category: ChallengeCategory,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val difficulty: ChallengeDifficulty,

    @Column(nullable = false)
    val duration: Int, // days

    @Column(name = "start_date", nullable = false)
    val startDate: LocalDate,

    @Column(name = "end_date", nullable = false)
    val endDate: LocalDate,

    @Column(name = "max_members", nullable = false)
    val maxMembers: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    val leader: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ChallengeStatus = ChallengeStatus.RECRUITING,

    @Column(name = "cover_image_url")
    val coverImageUrl: String? = null,

    @Column(name = "reward", length = 500)
    val reward: String? = null,

    @Column(name = "is_private", nullable = false)
    val isPrivate: Boolean = false,

    @Column(name = "invite_code", length = 8)
    val inviteCode: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "leader_role", nullable = false)
    val leaderRole: LeaderRole = LeaderRole.PARTICIPANT,

    @ElementCollection
    @CollectionTable(name = "challenge_tags", joinColumns = [JoinColumn(name = "challenge_id")])
    @Column(name = "tag")
    val tags: Set<String> = emptySet()
) : BaseEntity()

enum class ChallengeCategory {
    HEALTH,     // Maps to 'health'
    STUDY,      // Maps to 'study'
    HABIT,      // Maps to 'habit'
    HOBBY,      // Maps to 'hobby'
    SOCIAL,     // Maps to 'social'
    BUSINESS    // Maps to 'business'
}

enum class ChallengeDifficulty {
    EASY,       // Maps to 'easy'
    MEDIUM,     // Maps to 'medium'
    HARD        // Maps to 'hard'
}

enum class ChallengeStatus {
    RECRUITING, // Maps to 'recruiting'
    ACTIVE,     // Maps to 'active'
    COMPLETED   // Maps to 'completed'
}

enum class LeaderRole {
    PARTICIPANT, // Leader participates as a member in the challenge
    MANAGER      // Leader manages but doesn't participate as a member
}

// Extension methods for Challenge entity
fun Challenge.start() {
    this.status = ChallengeStatus.ACTIVE
}

fun Challenge.complete() {
    this.status = ChallengeStatus.COMPLETED
}
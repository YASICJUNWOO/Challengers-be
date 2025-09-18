package com.habitchallenge.domain.challengeapplication

import com.habitchallenge.domain.challenge.Challenge
import com.habitchallenge.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ChallengeApplicationRepository : JpaRepository<ChallengeApplication, Long> {

    fun findByUserAndChallenge(user: User, challenge: Challenge): ChallengeApplication?

    fun findByUserAndStatus(user: User, status: ApplicationStatus, pageable: Pageable): Page<ChallengeApplication>

    fun findByUser(user: User, pageable: Pageable): Page<ChallengeApplication>

    fun findByChallengeAndStatus(challenge: Challenge, status: ApplicationStatus, pageable: Pageable): Page<ChallengeApplication>

    fun findByChallenge(challenge: Challenge, pageable: Pageable): Page<ChallengeApplication>

    @Query("SELECT COUNT(ga) FROM ChallengeApplication ga WHERE ga.challenge = :challenge AND ga.status = :status")
    fun countByChallengeAndStatus(
        @Param("challenge") challenge: Challenge,
        @Param("status") status: ApplicationStatus
    ): Long

    @Query("SELECT ga FROM ChallengeApplication ga WHERE ga.challenge.id = :challengeId AND ga.status = 'PENDING'")
    fun findPendingApplicationsByChallengeId(@Param("challengeId") challengeId: Long): List<ChallengeApplication>

    fun existsByUserAndChallenge(user: User, challenge: Challenge): Boolean

    fun existsByUserAndChallengeAndStatus(user: User, challenge: Challenge, status: ApplicationStatus): Boolean

    @Query("SELECT ga FROM ChallengeApplication ga WHERE ga.user.id = :userId AND ga.status IN :statuses")
    fun findByUserIdAndStatusIn(
        @Param("userId") userId: Long,
        @Param("statuses") statuses: List<ApplicationStatus>,
        pageable: Pageable
    ): Page<ChallengeApplication>
}
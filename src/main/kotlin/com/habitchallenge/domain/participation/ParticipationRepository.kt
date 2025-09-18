package com.habitchallenge.domain.participation

import com.habitchallenge.domain.challenge.Challenge
import com.habitchallenge.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ParticipationRepository : JpaRepository<Participation, Long> {

    fun findByUserAndChallenge(user: User, challenge: Challenge): Participation?

    fun findByUserAndStatus(user: User, status: ParticipationStatus, pageable: Pageable): Page<Participation>

    fun findByUserAndStatus(user: User, status: ParticipationStatus): List<Participation>

    fun findByChallengeAndStatus(challenge: Challenge, status: ParticipationStatus): List<Participation>

    @Query("SELECT COUNT(p) FROM Participation p WHERE p.challenge = :challenge AND p.status = :status")
    fun countByChallengeAndStatus(
        @Param("challenge") challenge: Challenge,
        @Param("status") status: ParticipationStatus
    ): Long

    @Query("SELECT p FROM Participation p WHERE p.challenge.id = :challengeId AND p.status = 'JOINED'")
    fun findActiveParticipantsByChallengeId(@Param("challengeId") challengeId: Long): List<Participation>

    fun existsByUserAndChallenge(user: User, challenge: Challenge): Boolean

    fun existsByUserAndChallengeAndStatus(user: User, challenge: Challenge, status: ParticipationStatus): Boolean
}
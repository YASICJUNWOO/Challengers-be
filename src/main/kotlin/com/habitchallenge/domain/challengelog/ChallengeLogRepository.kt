package com.habitchallenge.domain.challengelog

import com.habitchallenge.domain.challenge.Challenge
import com.habitchallenge.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ChallengeLogRepository : JpaRepository<ChallengeLog, Long> {

    fun findByUserAndChallenge(user: User, challenge: Challenge): List<ChallengeLog>

    fun findByChallengeAndStatus(challenge: Challenge, status: LogStatus, pageable: Pageable): Page<ChallengeLog>

    fun findByUserAndStatus(user: User, status: LogStatus, pageable: Pageable): Page<ChallengeLog>

    fun findByStatus(status: LogStatus, pageable: Pageable): Page<ChallengeLog>

    @Query("SELECT cl FROM ChallengeLog cl WHERE cl.challenge = :challenge AND cl.status = 'APPROVED' ORDER BY cl.createdAt DESC")
    fun findApprovedLogsByChallenge(@Param("challenge") challenge: Challenge, pageable: Pageable): Page<ChallengeLog>

    @Query("SELECT cl FROM ChallengeLog cl WHERE cl.user = :user AND cl.status = 'APPROVED' ORDER BY cl.createdAt DESC")
    fun findApprovedLogsByUser(@Param("user") user: User, pageable: Pageable): Page<ChallengeLog>

    @Query("SELECT cl FROM ChallengeLog cl WHERE cl.challenge = :challenge AND cl.status = 'PENDING' ORDER BY cl.createdAt ASC")
    fun findPendingLogsByChallenge(@Param("challenge") challenge: Challenge): List<ChallengeLog>

    @Query("""
        SELECT cl FROM ChallengeLog cl
        WHERE (:challengeId IS NULL OR cl.challenge.id = :challengeId)
        AND (:userId IS NULL OR cl.user.id = :userId)
        AND (:status IS NULL OR cl.status = :status)
        ORDER BY cl.createdAt DESC
    """)
    fun findWithFilters(
        @Param("challengeId") challengeId: Long?,
        @Param("userId") userId: Long?,
        @Param("status") status: LogStatus?,
        pageable: Pageable
    ): Page<ChallengeLog>

    // New methods for scheduler service
    fun existsByUserAndChallengeAndCreatedAtBetween(
        user: User,
        challenge: Challenge,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): Boolean

    fun countByChallengeAndStatus(challenge: Challenge, status: LogStatus): Long

    @Modifying
    @Query("DELETE FROM ChallengeLog cl WHERE cl.challenge = :challenge")
    fun deleteByChallenge(@Param("challenge") challenge: Challenge)
}
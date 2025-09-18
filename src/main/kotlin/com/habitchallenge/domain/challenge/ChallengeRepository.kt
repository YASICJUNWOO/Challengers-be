package com.habitchallenge.domain.challenge

import com.habitchallenge.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ChallengeRepository : JpaRepository<Challenge, Long> {

    fun findByStatus(status: ChallengeStatus, pageable: Pageable): Page<Challenge>

    fun findByCategory(category: ChallengeCategory, pageable: Pageable): Page<Challenge>

    fun findByCategoryAndStatus(
        category: ChallengeCategory,
        status: ChallengeStatus,
        pageable: Pageable
    ): Page<Challenge>

    fun findByLeader(leader: User, pageable: Pageable): Page<Challenge>

    // Private challenge methods
    fun findByIsPrivate(isPrivate: Boolean, pageable: Pageable): Page<Challenge>

    fun findByCategoryAndIsPrivate(category: ChallengeCategory, isPrivate: Boolean, pageable: Pageable): Page<Challenge>

    fun findByStatusAndIsPrivate(status: ChallengeStatus, isPrivate: Boolean, pageable: Pageable): Page<Challenge>

    fun findByCategoryAndStatusAndIsPrivate(
        category: ChallengeCategory,
        status: ChallengeStatus,
        isPrivate: Boolean,
        pageable: Pageable
    ): Page<Challenge>

    fun findByInviteCode(inviteCode: String): Challenge?

    @Query(
        """
        SELECT g FROM Challenge g
        WHERE g.status = :status
        AND (LOWER(g.name) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(g.description) LIKE LOWER(CONCAT('%', :search, '%')))
    """
    )
    fun findByStatusAndSearch(
        @Param("status") status: ChallengeStatus,
        @Param("search") search: String,
        pageable: Pageable
    ): Page<Challenge>

    @Query(
        """
        SELECT g FROM Challenge g
        WHERE (LOWER(g.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(g.description) LIKE LOWER(CONCAT('%', :search, '%')))
    """
    )
    fun findBySearch(@Param("search") search: String, pageable: Pageable): Page<Challenge>

    @Query(
        """
        SELECT g FROM Challenge g
        WHERE g.status = :status
        AND g.isPrivate = :isPrivate
        AND (LOWER(g.name) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(g.description) LIKE LOWER(CONCAT('%', :search, '%')))
    """
    )
    fun findByStatusAndSearchAndIsPrivate(
        @Param("status") status: ChallengeStatus,
        @Param("search") search: String,
        @Param("isPrivate") isPrivate: Boolean,
        pageable: Pageable
    ): Page<Challenge>

    @Query(
        """
        SELECT g FROM Challenge g
        WHERE g.isPrivate = :isPrivate
        AND (LOWER(g.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(g.description) LIKE LOWER(CONCAT('%', :search, '%')))
    """
    )
    fun findBySearchAndIsPrivate(
        @Param("search") search: String,
        @Param("isPrivate") isPrivate: Boolean,
        pageable: Pageable
    ): Page<Challenge>

    // Methods for scheduler service
    fun findByStatus(status: ChallengeStatus): List<Challenge>
    fun findByStartDate(startDate: LocalDate): List<Challenge>
    fun findByEndDate(endDate: LocalDate): List<Challenge>
}
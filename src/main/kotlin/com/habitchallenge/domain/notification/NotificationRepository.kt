package com.habitchallenge.domain.notification

import com.habitchallenge.domain.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {

    fun findByUserOrderByCreatedAtDesc(user: User, pageable: Pageable): Page<Notification>

    fun findByUserAndIsReadOrderByCreatedAtDesc(user: User, isRead: Boolean, pageable: Pageable): Page<Notification>

    fun findByUserAndTypeOrderByCreatedAtDesc(user: User, type: NotificationType, pageable: Pageable): Page<Notification>

    fun findByUserAndIsReadAndTypeOrderByCreatedAtDesc(
        user: User,
        isRead: Boolean,
        type: NotificationType,
        pageable: Pageable
    ): Page<Notification>

    fun countByUserAndIsRead(user: User, isRead: Boolean): Long

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
    fun markAllAsReadByUser(@Param("user") user: User): Int

    fun existsByIdAndUser(id: Long, user: User): Boolean

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.relatedId = :challengeId")
    fun deleteByRelatedId(@Param("challengeId") challengeId: String)
}
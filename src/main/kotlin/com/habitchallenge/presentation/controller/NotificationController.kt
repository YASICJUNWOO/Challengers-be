package com.habitchallenge.presentation.controller

import com.habitchallenge.application.service.NotificationService
import com.habitchallenge.domain.notification.NotificationType
import com.habitchallenge.domain.user.User
import com.habitchallenge.presentation.dto.CreateNotificationRequest
import com.habitchallenge.presentation.dto.MarkAllReadResponse
import com.habitchallenge.presentation.dto.NotificationResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(private val notificationService: NotificationService) {

    @GetMapping
    fun getNotifications(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) read: Boolean?,
        @RequestParam(required = false) type: String?,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<Page<NotificationResponse>> {
        val pageable = PageRequest.of(page, size)

        val notifications = when {
            read != null && type != null -> {
                val notificationType = type.uppercase().replace("-", "_").let {
                    NotificationType.valueOf(it)
                }
                notificationService.findUserNotificationsByReadStatusAndType(user, read, notificationType, pageable)
            }
            read != null -> notificationService.findUserNotificationsByReadStatus(user, read, pageable)
            type != null -> {
                val notificationType = type.uppercase().replace("-", "_").let {
                    NotificationType.valueOf(it)
                }
                notificationService.findUserNotificationsByType(user, notificationType, pageable)
            }
            else -> notificationService.findUserNotifications(user, pageable)
        }

        val notificationResponses = notifications.map { NotificationResponse.from(it) }
        return ResponseEntity.ok(notificationResponses)
    }

    @PutMapping("/{id}/read")
    fun markNotificationAsRead(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<NotificationResponse> {
        val notification = notificationService.markAsRead(id, user)
        return ResponseEntity.ok(NotificationResponse.from(notification))
    }

    @PutMapping("/mark-all-read")
    fun markAllNotificationsAsRead(
        @AuthenticationPrincipal user: User
    ): ResponseEntity<MarkAllReadResponse> {
        val updatedCount = notificationService.markAllAsRead(user)
        return ResponseEntity.ok(MarkAllReadResponse(updatedCount))
    }

    @PostMapping
    fun createNotification(
        @Valid @RequestBody request: CreateNotificationRequest
    ): ResponseEntity<NotificationResponse> {
        val notification = notificationService.createNotification(
            userId = request.userId.toLong(),
            type = request.toNotificationType(),
            title = request.title,
            message = request.message,
            relatedId = request.relatedId,
            actionUrl = request.actionUrl
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(NotificationResponse.from(notification))
    }

    @DeleteMapping("/{id}")
    fun deleteNotification(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<Void> {
        notificationService.deleteNotification(id, user)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/unread-count")
    fun getUnreadCount(
        @AuthenticationPrincipal user: User
    ): ResponseEntity<Map<String, Long>> {
        val count = notificationService.countUnreadNotifications(user)
        return ResponseEntity.ok(mapOf("unreadCount" to count))
    }
}
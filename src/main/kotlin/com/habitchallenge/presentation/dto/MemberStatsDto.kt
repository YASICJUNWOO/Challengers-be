package com.habitchallenge.presentation.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class MemberStatsResponse(
    val memberId: String,
    val streak: Int,
    val achievementRate: Double,
    val totalSubmissions: Int,
    val approvedSubmissions: Int,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val lastSubmissionDate: LocalDateTime?
)

data class ParticipationStatsResponse(
    val date: String, // YYYY-MM-DD format
    val participated: Boolean? = null, // Only when userId filter is applied
    val participationRate: Double, // 0-100
    val submissions: Int,
    val userCount: Int
)
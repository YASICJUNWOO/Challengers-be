package com.habitchallenge.application.service

import com.habitchallenge.domain.certification.Certification
import com.habitchallenge.domain.certification.CertificationRepository
import com.habitchallenge.domain.certification.CertificationStatus
import com.habitchallenge.domain.challenge.ChallengeBatch
import com.habitchallenge.domain.challenge.ChallengeBatchRepository
import com.habitchallenge.domain.challenge.ChallengeBatchStatus
import com.habitchallenge.domain.participation.Participation
import com.habitchallenge.domain.participation.ParticipationRepository
import com.habitchallenge.domain.participation.ParticipationStatus
import com.habitchallenge.domain.user.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.*

class CertificationServiceTest {

    private lateinit var certificationRepository: CertificationRepository
    private lateinit var challengeBatchRepository: ChallengeBatchRepository
    private lateinit var participationRepository: ParticipationRepository
    private lateinit var certificationService: CertificationService

    @BeforeEach
    fun setUp() {
        certificationRepository = mockk()
        challengeBatchRepository = mockk()
        participationRepository = mockk()
        certificationService = CertificationService(
            certificationRepository,
            challengeBatchRepository,
            participationRepository
        )
    }

    @Test
    fun `인증 생성 성공`() {
        // Given
        val user = mockk<User> { every { id } returns 1L }
        val batchId = 1L
        val content = "Today's workout completed!"
        val imageUrl = "http://example.com/image.jpg"
        val certificationDate = LocalDate.now()

        val batch = mockk<ChallengeBatch> {
            every { id } returns batchId
            every { isActive() } returns true
        }

        val participation = mockk<Participation> {
            every { isApproved() } returns true
        }

        every { challengeBatchRepository.findById(batchId) } returns Optional.of(batch)
        every { participationRepository.findByUserAndChallengeBatch(user, batch) } returns participation
        every { certificationRepository.existsByUserAndChallengeBatchAndCertificationDate(user, batch, certificationDate) } returns false
        every { certificationRepository.save(any()) } returns mockk<Certification> {
            every { id } returns 1L
            every { this@mockk.user } returns user
            every { challengeBatch } returns batch
            every { this@mockk.content } returns content
            every { status } returns CertificationStatus.PENDING
        }

        // When
        val result = certificationService.createCertification(user, batchId, content, imageUrl, certificationDate)

        // Then
        assertNotNull(result)
        verify { challengeBatchRepository.findById(batchId) }
        verify { participationRepository.findByUserAndChallengeBatch(user, batch) }
        verify { certificationRepository.save(any()) }
    }

    @Test
    fun `비활성 챌린지에 인증 생성 실패`() {
        // Given
        val user = mockk<User> { every { id } returns 1L }
        val batchId = 1L
        val content = "Today's workout completed!"

        val batch = mockk<ChallengeBatch> {
            every { id } returns batchId
            every { isActive() } returns false
        }

        every { challengeBatchRepository.findById(batchId) } returns Optional.of(batch)

        // When & Then
        assertThrows<IllegalStateException> {
            certificationService.createCertification(user, batchId, content, null)
        }
    }

    @Test
    fun `인증 승인 성공`() {
        // Given
        val certificationId = 1L
        val reviewer = mockk<User> { every { id } returns 2L }
        val comment = "Great work!"

        val certification = mockk<Certification>(relaxed = true) {
            every { isPending() } returns true
        }

        every { certificationRepository.findById(certificationId) } returns Optional.of(certification)
        every { certificationRepository.save(certification) } returns certification

        // When
        val result = certificationService.approveCertification(certificationId, reviewer, comment)

        // Then
        assertEquals(certification, result)
        verify { certification.approve(reviewer, comment) }
        verify { certificationRepository.save(certification) }
    }

    @Test
    fun `이미 승인된 인증 승인 실패`() {
        // Given
        val certificationId = 1L
        val reviewer = mockk<User> { every { id } returns 2L }

        val certification = mockk<Certification> {
            every { isPending() } returns false
        }

        every { certificationRepository.findById(certificationId) } returns Optional.of(certification)

        // When & Then
        assertThrows<IllegalStateException> {
            certificationService.approveCertification(certificationId, reviewer)
        }
    }
}
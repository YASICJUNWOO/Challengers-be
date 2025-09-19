package com.habitchallenge.application.service

import com.habitchallenge.domain.challenge.*
import com.habitchallenge.domain.participation.Participation
import com.habitchallenge.domain.participation.ParticipationRepository
import com.habitchallenge.domain.participation.ParticipationStatus
import com.habitchallenge.domain.user.User
import com.habitchallenge.domain.user.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChallengeUpdateTest {

    @Autowired
    private lateinit var challengeService: ChallengeService

    @Autowired
    private lateinit var challengeRepository: ChallengeRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var participationRepository: ParticipationRepository

    @Test
    fun `챌린지 수정 시 FK 관계가 유지되는지 테스트`() {
        // Given: 사용자와 챌린지 생성
        val leader = User(
            loginId = "leader123",
            email = "leader@test.com",
            password = "password123",
            nickname = "Leader"
        )
        val savedLeader = userRepository.save(leader)

        val member = User(
            loginId = "member123",
            email = "member@test.com",
            password = "password123",
            nickname = "Member"
        )
        val savedMember = userRepository.save(member)

        // 챌린지 생성
        val challenge = challengeService.createChallenge(
            name = "Original Challenge",
            description = "Original Description",
            category = ChallengeCategory.HEALTH,
            difficulty = ChallengeDifficulty.EASY,
            duration = 30,
            startDate = LocalDate.now().plusDays(1),
            endDate = LocalDate.now().plusDays(31),
            maxMembers = 10,
            leader = savedLeader,
            leaderRole = LeaderRole.PARTICIPANT
        )

        // 멤버 추가
        challengeService.joinChallenge(savedMember, challenge.id!!, "참여 신청")

        val originalChallengeId = challenge.id!!

        // 챌린지 업데이트 전 참여자 수 확인
        val participationsBeforeUpdate = participationRepository.findActiveParticipantsByChallengeId(originalChallengeId)
        assertEquals(2, participationsBeforeUpdate.size) // 리더 + 멤버

        // When: 챌린지 수정
        val updatedChallenge = challengeService.updateChallenge(
            challengeId = originalChallengeId,
            currentUser = savedLeader,
            name = "Updated Challenge Name",
            description = "Updated Description",
            category = ChallengeCategory.STUDY,
            difficulty = null, // 기존 값 유지
            duration = null,   // 기존 값 유지
            startDate = null,  // 기존 값 유지
            endDate = null,    // 기존 값 유지
            maxMembers = 15,   // 변경
            coverImageUrl = null,
            reward = "Updated Reward",
            tags = setOf("tag1", "tag2")
        )

        // Then: 검증
        // 1. 챌린지 ID가 동일한지 확인 (delete-insert가 아닌 update인지 확인)
        assertEquals(originalChallengeId, updatedChallenge.id)

        // 2. 업데이트된 필드들이 올바르게 변경되었는지 확인
        assertEquals("Updated Challenge Name", updatedChallenge.name)
        assertEquals("Updated Description", updatedChallenge.description)
        assertEquals(ChallengeCategory.STUDY, updatedChallenge.category)
        assertEquals(15, updatedChallenge.maxMembers)
        assertEquals("Updated Reward", updatedChallenge.reward)
        assertEquals(setOf("tag1", "tag2"), updatedChallenge.tags)

        // 3. 변경하지 않은 필드들이 유지되었는지 확인
        assertEquals(ChallengeDifficulty.EASY, updatedChallenge.difficulty)
        assertEquals(30, updatedChallenge.duration)

        // 4. FK 관계가 유지되었는지 확인 - 참여자 수가 그대로인지
        val participationsAfterUpdate = participationRepository.findActiveParticipantsByChallengeId(originalChallengeId)
        assertEquals(2, participationsAfterUpdate.size)

        // 5. 각 참여자의 챌린지 ID가 여전히 올바른지 확인
        participationsAfterUpdate.forEach { participation ->
            assertEquals(originalChallengeId, participation.challenge.id)
        }

        // 6. 데이터베이스에서 직접 조회해서 확인
        val challengeFromDb = challengeRepository.findById(originalChallengeId).orElse(null)
        assertNotNull(challengeFromDb)
        assertEquals("Updated Challenge Name", challengeFromDb!!.name)
        assertEquals("Updated Description", challengeFromDb.description)
    }

    @Test
    fun `챌린지 수정 시 존재하지 않는 챌린지일 경우 예외 발생`() {
        // Given
        val leader = User(
            loginId = "leader456",
            email = "leader2@test.com",
            password = "password123",
            nickname = "Leader2"
        )
        val savedLeader = userRepository.save(leader)

        // When & Then
        assertThrows(NoSuchElementException::class.java) {
            challengeService.updateChallenge(
                challengeId = 999999L,
                currentUser = savedLeader,
                name = "Should Fail",
                description = null,
                category = null,
                difficulty = null,
                duration = null,
                startDate = null,
                endDate = null,
                maxMembers = null,
                coverImageUrl = null,
                reward = null,
                tags = null
            )
        }
    }

    @Test
    fun `챌린지 수정 시 리더가 아닌 사용자가 수정 시도할 경우 예외 발생`() {
        // Given
        val leader = User(
            loginId = "leader789",
            email = "leader3@test.com",
            password = "password123",
            nickname = "Leader3"
        )
        val savedLeader = userRepository.save(leader)

        val notLeader = User(
            loginId = "notleader123",
            email = "notleader@test.com",
            password = "password123",
            nickname = "NotLeader"
        )
        val savedNotLeader = userRepository.save(notLeader)

        val challenge = challengeService.createChallenge(
            name = "Test Challenge",
            description = "Test Description",
            category = ChallengeCategory.HEALTH,
            difficulty = ChallengeDifficulty.EASY,
            duration = 30,
            startDate = LocalDate.now().plusDays(1),
            endDate = LocalDate.now().plusDays(31),
            maxMembers = 10,
            leader = savedLeader
        )

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            challengeService.updateChallenge(
                challengeId = challenge.id!!,
                currentUser = savedNotLeader, // 리더가 아닌 사용자
                name = "Should Fail",
                description = null,
                category = null,
                difficulty = null,
                duration = null,
                startDate = null,
                endDate = null,
                maxMembers = null,
                coverImageUrl = null,
                reward = null,
                tags = null
            )
        }
    }
}
# Entity Specification

**Version**: 1.1.0
**Last Updated**: 2025-09-17
**Status**: Active

## Overview
This document defines the database entity models for the Challenge MVP backend. All entities follow JPA/Hibernate conventions and include audit fields through BaseEntity.

## Change Log
- **v1.1.0** (2025-09-17): Added GroupApplication entity for approval-based group joining
- **v1.0.0** (2025-09-15): Initial entity design based on frontend API requirements

---

## Core Entities

### 1. User Entity
```kotlin
@Entity
@Table(name = "users")
class User(
    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    private val password: String,

    @Column(nullable = false)
    val nickname: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole = UserRole.MEMBER,

    @Column(name = "avatar_url")
    val avatarUrl: String? = null,

    @Column(name = "is_active")
    val isActive: Boolean = true
) : BaseEntity(), UserDetails
```

**Enum: UserRole**
```kotlin
enum class UserRole {
    LEADER,    // Maps to 'leader' in frontend
    MEMBER     // Maps to 'member' in frontend
}
```

**Mapping to Frontend User Interface:**
- `id` → `id: string`
- `nickname` → `name: string`
- `email` → `email: string`
- `avatarUrl` → `avatar?: string`
- `role` → `role: 'leader' | 'member'`
- `createdAt` → `createdAt: string` (ISO format)

---

### 2. ChallengeGroup Entity
```kotlin
@Entity
@Table(name = "challenge_groups")
class ChallengeGroup(
    @Column(nullable = false)
    val name: String,

    @Column(length = 1000)
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
    val status: ChallengeStatus = ChallengeStatus.RECRUITING,

    @Column(name = "cover_image_url")
    val coverImageUrl: String? = null,

    @Column(name = "reward", length = 500)
    val reward: String? = null,

    @ElementCollection
    @CollectionTable(name = "challenge_tags", joinColumns = [JoinColumn(name = "group_id")])
    @Column(name = "tag")
    val tags: Set<String> = emptySet()
) : BaseEntity()
```

**Enums:**
```kotlin
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
```

**Mapping to Frontend Group Interface:**
- `id` → `id: string`
- `name` → `name: string`
- `description` → `description: string`
- `category` → `category: string` (lowercase)
- `difficulty` → `difficulty: string` (lowercase)
- `duration` → `duration: number`
- `startDate` → `startDate: string` (ISO date)
- `endDate` → `endDate: string` (ISO date)
- `maxMembers` → `maxMembers: number`
- `participations.count` → `currentMembers: number` (calculated)
- `leader.id` → `leaderId: string`
- `leader` → `leader: User`
- `participations.users` → `members: User[]` (joined users)
- `status` → `status: string` (lowercase)
- `coverImageUrl` → `coverImage?: string`
- `reward` → `reward?: string`
- `tags` → `tags?: string[]`
- `createdAt` → `createdAt: string` (ISO format)

---

### 3. Participation Entity
```kotlin
@Entity
@Table(
    name = "participations",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "group_id"])
    ]
)
class Participation(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    val group: ChallengeGroup,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ParticipationStatus = ParticipationStatus.JOINED,

    @Column(name = "join_reason", length = 500)
    val joinReason: String? = null
) : BaseEntity()
```

**Enum:**
```kotlin
enum class ParticipationStatus {
    JOINED,     // Active participation
    LEFT        // Left the group
}
```

**Purpose**: Tracks user membership in challenge groups
- Replaces the direct many-to-many relationship
- Enables future features like join requests, leave tracking
- Supports audit trail for group membership changes

---

### 4. GroupApplication Entity
```kotlin
@Entity
@Table(
    name = "group_applications",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "group_id"])
    ]
)
class GroupApplication(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    val group: ChallengeGroup,

    @Column(nullable = false, length = 500)
    val reason: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ApplicationStatus = ApplicationStatus.PENDING,

    @Column(name = "reviewed_at")
    var reviewedAt: LocalDateTime? = null,

    @Column(name = "rejection_reason", length = 500)
    var rejectionReason: String? = null
) : BaseEntity()
```

**Enum:**
```kotlin
enum class ApplicationStatus {
    PENDING,    // Maps to 'pending'
    APPROVED,   // Maps to 'approved'
    REJECTED    // Maps to 'rejected'
}
```

**Mapping to Frontend GroupApplication Interface:**
- `id` → `id: string`
- `group.id` → `groupId: string`
- `user.id` → `userId: string`
- `reason` → `reason: string`
- `status` → `status: string` (lowercase)
- `createdAt` → `createdAt: string` (ISO format)
- `reviewedAt` → `reviewedAt?: string` (ISO format)
- `rejectionReason` → `rejectionReason?: string`
- `user` → `user: { id, name, avatar }` (embedded user info)

**Purpose**: Tracks group join applications in approval-based workflow
- Replaces direct joining with approval process
- Enables group leaders to review and approve/reject applications
- Supports application reason and rejection feedback
- Automatically triggers group membership upon approval

---

### 5. ChallengeLog Entity
```kotlin
@Entity
@Table(name = "challenge_logs")
class ChallengeLog(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    val group: ChallengeGroup,

    @Column(nullable = false, length = 2000)
    val content: String,

    @Column(name = "image_url")
    val imageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: LogStatus = LogStatus.PENDING,

    @Column(name = "rejection_comment", length = 500)
    var rejectionComment: String? = null
) : BaseEntity()
```

**Enum:**
```kotlin
enum class LogStatus {
    PENDING,    // Maps to 'pending'
    APPROVED,   // Maps to 'approved'
    REJECTED    // Maps to 'rejected'
}
```

**Mapping to Frontend ChallengeLog Interface:**
- `id` → `id: string`
- `user.id` → `userId: string`
- `group.id` → `groupId: string`
- `content` → `content: string`
- `imageUrl` → `imageUrl?: string`
- `status` → `status: string` (lowercase)
- `rejectionComment` → `rejectionComment?: string`
- `user` → `user: { id, name, avatar }` (embedded user info)
- `createdAt` → `createdAt: string` (ISO format)

---

### 6. BaseEntity (Abstract)
```kotlin
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BaseEntity
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
```

---

## Database Schema

### Table Relationships
```
users (1) ←→ (N) participations (N) ←→ (1) challenge_groups
users (1) ←→ (N) group_applications (N) ←→ (1) challenge_groups
users (1) ←→ (N) challenge_logs (N) ←→ (1) challenge_groups
challenge_groups (1) ←→ (1) users [leader_id]
```

### Foreign Key Constraints
- `participations.user_id` → `users.id`
- `participations.group_id` → `challenge_groups.id`
- `group_applications.user_id` → `users.id`
- `group_applications.group_id` → `challenge_groups.id`
- `challenge_logs.user_id` → `users.id`
- `challenge_logs.group_id` → `challenge_groups.id`
- `challenge_groups.leader_id` → `users.id`

### Unique Constraints
- `users.email` (unique)
- `participations(user_id, group_id)` (composite unique)
- `group_applications(user_id, group_id)` (composite unique)

---

## Migration Notes

### From Frontend Mock API
1. **User.name** → **User.nickname**: Field renamed for clarity
2. **Group.leaderId** → **Group.leader**: Changed from ID reference to entity relationship
3. **Group.members[]** → **Participations**: Changed from embedded array to separate entity
4. **Group.currentMembers** → **Calculated**: Derived from participation count
5. **Added audit fields**: All entities now have `created_at`, `updated_at`
6. **Added status tracking**: Participations and logs have status fields

### String ID to Long ID
- Frontend uses string IDs: `"1", "2", "3"`
- Backend uses Long IDs: `1L, 2L, 3L`
- API responses convert Long → String for frontend compatibility

---

## Validation Rules

### User Entity
- Email: Must be valid email format, unique
- Nickname: 2-50 characters, required
- Password: 8-100 characters (encrypted)

### ChallengeGroup Entity
- Name: 2-100 characters, required
- Description: 10-1000 characters, required
- Duration: 1-365 days
- MaxMembers: 2-1000 people
- StartDate: Must be future date
- EndDate: Must be after startDate

### ChallengeLog Entity
- Content: 10-2000 characters, required
- ImageUrl: Valid URL format if provided

### GroupApplication Entity
- Reason: 10-500 characters, required
- Status: Must be one of [PENDING, APPROVED, REJECTED]
- RejectionReason: 10-500 characters if status is REJECTED

---

## Version History
- **v1.1.0** (2025-09-17): Added GroupApplication entity for approval-based group joining workflow
- **v1.0.0** (2025-09-15): Initial entity design with full frontend API compatibility
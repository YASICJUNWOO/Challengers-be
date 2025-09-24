package com.habitchallenge.domain.user

import com.habitchallenge.domain.common.BaseEntity
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "users")
class User(
    @Column(unique = true, nullable = false)
    var email: String,

    @Column(unique = true, nullable = false, name = "login_id")
    val loginId: String,

    @Column(nullable = true)  // Google OAuth 사용자는 비밀번호가 없을 수 있음
    private var password: String? = null,

    @Column(nullable = false)
    var nickname: String,  // Maps to "name" in API response

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole = UserRole.MEMBER,

    @Column(name = "avatar_url")
    val avatarUrl: String? = null,  // Maps to "avatar" in API response

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "google_id", unique = true)
    val googleId: String? = null,  // Google OAuth 사용자 ID

    @Column(name = "auth_provider")
    @Enumerated(EnumType.STRING)
    val authProvider: AuthProvider = AuthProvider.LOCAL  // 인증 제공자 (LOCAL/GOOGLE)
) : BaseEntity(), UserDetails {

    // Computed property for loginType based on authProvider
    val loginType: String get() = when (authProvider) {
        AuthProvider.LOCAL -> "local"
        AuthProvider.GOOGLE -> "social"
    }

    // Computed property for provider (for frontend compatibility)
    val provider: String? get() = when (authProvider) {
        AuthProvider.LOCAL -> null
        AuthProvider.GOOGLE -> "google"
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
    }

    override fun getPassword(): String = password ?: ""
    override fun getUsername(): String = loginId
    override fun isAccountNonExpired(): Boolean = isActive
    override fun isAccountNonLocked(): Boolean = isActive
    override fun isCredentialsNonExpired(): Boolean = isActive
    override fun isEnabled(): Boolean = isActive

    // Convenience property for notification service
    val name: String get() = nickname

    // Public accessor for password (needed for UserService)
    fun getPasswordString(): String = password ?: ""

    // Methods to update fields
    fun updatePassword(newPassword: String) {
        this.password = newPassword
    }

    fun updateProfile(newNickname: String? = null, newEmail: String? = null) {
        newNickname?.let { this.nickname = it }
        newEmail?.let { this.email = it }
    }
}

enum class UserRole {
    LEADER,    // Maps to 'leader' in frontend
    MEMBER     // Maps to 'member' in frontend
}

enum class AuthProvider {
    LOCAL,     // 일반 로그인 (이메일/비밀번호)
    GOOGLE     // Google OAuth 로그인
}
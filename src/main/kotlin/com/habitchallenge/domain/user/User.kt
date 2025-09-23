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
    val email: String,

    @Column(unique = true, nullable = false, name = "login_id")
    val loginId: String,

    @Column(nullable = true)  // Google OAuth 사용자는 비밀번호가 없을 수 있음
    private val password: String? = null,

    @Column(nullable = false)
    val nickname: String,  // Maps to "name" in API response

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
}

enum class UserRole {
    LEADER,    // Maps to 'leader' in frontend
    MEMBER     // Maps to 'member' in frontend
}

enum class AuthProvider {
    LOCAL,     // 일반 로그인 (이메일/비밀번호)
    GOOGLE     // Google OAuth 로그인
}
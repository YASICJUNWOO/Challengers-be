package com.habitchallenge.domain.user

import com.habitchallenge.domain.common.BaseEntity
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "users")
class User(
    @Column(unique = true, nullable = false, name = "login_id")
    val loginId: String,

    @Column(nullable = false)
    private val password: String,

    @Column(nullable = false)
    val nickname: String,  // Maps to "name" in API response

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole = UserRole.MEMBER,

    @Column(name = "avatar_url")
    val avatarUrl: String? = null,  // Maps to "avatar" in API response

    @Column(name = "is_active")
    val isActive: Boolean = true
) : BaseEntity(), UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
    }

    override fun getPassword(): String = password
    override fun getUsername(): String = loginId
    override fun isAccountNonExpired(): Boolean = isActive
    override fun isAccountNonLocked(): Boolean = isActive
    override fun isCredentialsNonExpired(): Boolean = isActive
    override fun isEnabled(): Boolean = isActive

    // Convenience property for notification service
    val name: String get() = nickname

    // Public accessor for password (needed for UserService)
    fun getPasswordString(): String = password
}

enum class UserRole {
    LEADER,    // Maps to 'leader' in frontend
    MEMBER     // Maps to 'member' in frontend
}
package com.habitchallenge.application.service

import com.habitchallenge.domain.user.AuthProvider
import com.habitchallenge.domain.user.User
import com.habitchallenge.domain.user.UserRepository
import com.habitchallenge.domain.user.UserRole
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun createUser(email: String, loginId: String, password: String, nickname: String, role: UserRole = UserRole.MEMBER): User {
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("이미 존재하는 이메일입니다.")
        }

        if (userRepository.existsByLoginId(loginId)) {
            throw IllegalArgumentException("이미 존재하는 로그인 ID입니다.")
        }

        if (userRepository.existsByNickname(nickname)) {
            throw IllegalArgumentException("이미 존재하는 닉네임입니다.")
        }

        val encodedPassword = passwordEncoder.encode(password)
        val user = User(
            email = email,
            loginId = loginId,
            password = encodedPassword,
            nickname = nickname,
            role = role
        )

        return userRepository.save(user)
    }

    fun findById(id: Long): User {
        return userRepository.findById(id)
            .orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다: $id") }
    }

    fun findByLoginId(loginId: String): User {
        return userRepository.findByLoginId(loginId)
            .orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다: $loginId") }
    }

    fun validatePassword(user: User, rawPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, user.getPasswordString())
    }

    fun findAll(pageable: Pageable): Page<User> {
        return userRepository.findAll(pageable)
    }

    fun findByRole(role: UserRole, pageable: Pageable): Page<User> {
        return userRepository.findByRole(role, pageable)
    }

    /**
     * Google OAuth 사용자 생성 또는 기존 사용자 반환
     * @param googleUserInfo Google에서 받은 사용자 정보
     * @return 생성되거나 기존 사용자
     */
    @Transactional
    fun findOrCreateGoogleUser(googleUserInfo: GoogleUserInfo): User {
        // 1. Google ID로 기존 사용자 찾기
        val existingUserByGoogleId = userRepository.findByGoogleId(googleUserInfo.googleId)
        if (existingUserByGoogleId.isPresent) {
            return existingUserByGoogleId.get()
        }

        // 2. 이메일로 기존 사용자 찾기 (이미 다른 방법으로 가입한 경우)
        val existingUserByEmail = userRepository.findByEmail(googleUserInfo.email)
        if (existingUserByEmail.isPresent) {
            throw IllegalArgumentException("해당 이메일로 이미 가입된 계정이 있습니다. 기존 계정으로 로그인해주세요.")
        }

        // 3. 새 Google 사용자 생성
        val loginId = generateUniqueLoginId(googleUserInfo.email)
        val user = User(
            email = googleUserInfo.email,
            loginId = loginId,
            password = null, // Google OAuth 사용자는 비밀번호 없음
            nickname = googleUserInfo.name.ifBlank { "사용자" },
            role = UserRole.MEMBER, // 기본값은 MEMBER
            avatarUrl = googleUserInfo.picture,
            googleId = googleUserInfo.googleId,
            authProvider = AuthProvider.GOOGLE
        )

        return userRepository.save(user)
    }

    /**
     * 고유한 로그인 ID 생성 (이메일 기반)
     * @param email 사용자 이메일
     * @return 고유한 로그인 ID
     */
    private fun generateUniqueLoginId(email: String): String {
        val baseLoginId = email.substringBefore("@")
        var loginId = baseLoginId
        var counter = 1

        while (userRepository.existsByLoginId(loginId)) {
            loginId = "${baseLoginId}${counter}"
            counter++
        }

        return loginId
    }
}
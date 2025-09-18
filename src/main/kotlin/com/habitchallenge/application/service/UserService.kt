package com.habitchallenge.application.service

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
    fun createUser(loginId: String, password: String, nickname: String, role: UserRole = UserRole.MEMBER): User {
        if (userRepository.existsByLoginId(loginId)) {
            throw IllegalArgumentException("이미 존재하는 로그인 ID입니다.")
        }

        if (userRepository.existsByNickname(nickname)) {
            throw IllegalArgumentException("이미 존재하는 닉네임입니다.")
        }

        val encodedPassword = passwordEncoder.encode(password)
        val user = User(
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
}
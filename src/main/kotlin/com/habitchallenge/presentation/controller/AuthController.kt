package com.habitchallenge.presentation.controller

import com.habitchallenge.application.service.UserService
import com.habitchallenge.domain.user.UserRole
import com.habitchallenge.infrastructure.security.JwtTokenProvider
import com.habitchallenge.presentation.dto.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<AuthResponse> {
        val user = userService.createUser(
            email = request.email,
            loginId = request.loginId,
            password = request.password,
            nickname = request.nickname,
            role = UserRole.valueOf(request.role)
        )

        val token = jwtTokenProvider.generateToken(user)
        val response = AuthResponse(
            token = token,
            user = UserResponse.from(user)
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/signin")
    fun signin(@Valid @RequestBody request: SigninRequest): ResponseEntity<AuthResponse> {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.id, request.password)
        )

        val user = authentication.principal as com.habitchallenge.domain.user.User
        val token = jwtTokenProvider.generateToken(authentication)
        val response = AuthResponse(
            token = token,
            user = UserResponse.from(user)
        )

        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<Map<String, String>> {
        // JWT는 Stateless이므로 서버에서 토큰을 무효화할 수 없음
        // 클라이언트에서 토큰을 삭제하도록 응답만 반환
        val response = mapOf("message" to "로그아웃되었습니다.")
        return ResponseEntity.ok(response)
    }
}
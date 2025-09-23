package com.habitchallenge.presentation.controller

import com.habitchallenge.application.service.GoogleAuthService
import com.habitchallenge.application.service.GoogleTokenValidationException
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
    private val jwtTokenProvider: JwtTokenProvider,
    private val googleAuthService: GoogleAuthService
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

    @PostMapping("/google")
    fun googleLogin(@Valid @RequestBody request: GoogleLoginRequest): ResponseEntity<*> {
        return try {
            // 1. Google 토큰 검증 및 사용자 정보 추출
            val googleUserInfo = googleAuthService.verifyToken(request.token)

            // 2. 사용자 생성 또는 기존 사용자 찾기
            val user = userService.findOrCreateGoogleUser(googleUserInfo)

            // 3. JWT 토큰 생성
            val token = jwtTokenProvider.generateToken(user)

            // 4. 응답 생성
            val response = AuthResponse(
                token = token,
                user = UserResponse.from(user)
            )

            ResponseEntity.ok(response)
        } catch (e: GoogleTokenValidationException) {
            // Google 토큰 검증 실패
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "토큰 검증 실패", "message" to e.message))
        } catch (e: IllegalArgumentException) {
            // 이미 존재하는 이메일 등의 비즈니스 로직 오류
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "잘못된 요청", "message" to e.message))
        } catch (e: Exception) {
            // 기타 서버 오류
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "서버 오류", "message" to "Google 로그인 처리 중 오류가 발생했습니다."))
        }
    }
}
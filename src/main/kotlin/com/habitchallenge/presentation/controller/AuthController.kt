package com.habitchallenge.presentation.controller

import com.habitchallenge.application.service.GoogleAuthService
import com.habitchallenge.application.service.GoogleTokenValidationException
import com.habitchallenge.application.service.PasswordResetService
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
    private val googleAuthService: GoogleAuthService,
    private val passwordResetService: PasswordResetService
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

    @PostMapping("/password/reset/request")
    fun requestPasswordReset(@Valid @RequestBody request: PasswordResetRequest): ResponseEntity<*> {
        return try {
            passwordResetService.sendResetCode(request.email)
            ResponseEntity.ok(PasswordResetResponse("인증코드가 이메일로 발송되었습니다."))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(PasswordResetResponse("등록되지 않은 이메일입니다.", false))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(400).body(PasswordResetResponse(e.message ?: "잘못된 요청입니다.", false))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(429).body(PasswordResetResponse(e.message ?: "요청이 너무 많습니다.", false))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(PasswordResetResponse("서버 오류가 발생했습니다.", false))
        }
    }

    @PostMapping("/password/reset/verify")
    fun verifyPasswordResetCode(@Valid @RequestBody request: PasswordResetVerifyRequest): ResponseEntity<*> {
        return try {
            val token = passwordResetService.verifyCodeAndGenerateToken(request.email, request.code)
            ResponseEntity.ok(PasswordResetVerifyResponse("인증이 완료되었습니다.", token))
        } catch (e: IllegalArgumentException) {
            if (e.message?.contains("만료") == true) {
                ResponseEntity.status(410).body(PasswordResetVerifyResponse(e.message ?: "만료된 코드입니다.", "", false))
            } else {
                ResponseEntity.status(400).body(PasswordResetVerifyResponse(e.message ?: "잘못된 인증코드입니다.", "", false))
            }
        } catch (e: Exception) {
            ResponseEntity.status(500).body(PasswordResetVerifyResponse("서버 오류가 발생했습니다.", "", false))
        }
    }

    @PostMapping("/password/reset/confirm")
    fun confirmPasswordReset(@Valid @RequestBody request: PasswordResetConfirmRequest): ResponseEntity<*> {
        return try {
            val temporaryPassword = passwordResetService.confirmResetAndGenerateTemporaryPassword(request.token)
            ResponseEntity.ok(PasswordResetConfirmResponse(
                "임시 비밀번호가 이메일로 발송되었습니다. 로그인 후 반드시 비밀번호를 변경해주세요.",
                temporaryPassword
            ))
        } catch (e: IllegalArgumentException) {
            if (e.message?.contains("만료") == true) {
                ResponseEntity.status(410).body(PasswordResetConfirmResponse(e.message ?: "만료된 토큰입니다.", "", false))
            } else {
                ResponseEntity.status(400).body(PasswordResetConfirmResponse(e.message ?: "유효하지 않은 토큰입니다.", "", false))
            }
        } catch (e: Exception) {
            ResponseEntity.status(500).body(PasswordResetConfirmResponse("서버 오류가 발생했습니다.", "", false))
        }
    }
}
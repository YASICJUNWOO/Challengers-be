package com.habitchallenge.application.service

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class GoogleAuthService(
    private val webClient: WebClient = WebClient.builder()
        .baseUrl("https://oauth2.googleapis.com")
        .build()
) {

    /**
     * Google OAuth2 ID 토큰을 검증하고 사용자 정보를 반환합니다.
     * @param idToken Google OAuth2 ID 토큰
     * @return GoogleUserInfo 사용자 정보
     * @throws GoogleTokenValidationException 토큰 검증 실패 시
     */
    fun verifyToken(idToken: String): GoogleUserInfo {
        return try {
            val response = webClient.get()
                .uri("/tokeninfo?id_token={token}", idToken)
                .retrieve()
                .bodyToMono(GoogleTokenResponse::class.java)
                .block() ?: throw GoogleTokenValidationException("Google API 응답이 비어있습니다.")

            // 토큰 유효성 검증
            if (response.aud.isNullOrBlank() || response.sub.isNullOrBlank()) {
                throw GoogleTokenValidationException("유효하지 않은 토큰입니다.")
            }

            GoogleUserInfo(
                googleId = response.sub,
                email = response.email ?: "",
                name = response.name ?: "",
                picture = response.picture
            )
        } catch (e: WebClientResponseException) {
            when (e.statusCode.value()) {
                400 -> throw GoogleTokenValidationException("잘못된 토큰 형식입니다.")
                else -> throw GoogleTokenValidationException("토큰 검증에 실패했습니다: ${e.message}")
            }
        } catch (e: Exception) {
            if (e is GoogleTokenValidationException) throw e
            throw GoogleTokenValidationException("Google API 호출 중 오류가 발생했습니다: ${e.message}")
        }
    }
}

/**
 * Google API 토큰 검증 응답
 */
data class GoogleTokenResponse(
    val aud: String? = null,           // 클라이언트 ID
    val sub: String? = null,           // Google 사용자 ID
    val email: String? = null,         // 이메일
    val email_verified: String? = null, // 이메일 검증 여부
    val name: String? = null,          // 이름
    val picture: String? = null,       // 프로필 이미지 URL
    val given_name: String? = null,    // 이름
    val family_name: String? = null,   // 성
    val locale: String? = null,        // 로케일
    val iat: Long? = null,             // 발급 시간
    val exp: Long? = null              // 만료 시간
)

/**
 * Google 사용자 정보
 */
data class GoogleUserInfo(
    val googleId: String,      // Google 사용자 고유 ID
    val email: String,         // 이메일
    val name: String,          // 이름
    val picture: String?       // 프로필 이미지 URL
)

/**
 * Google 토큰 검증 실패 예외
 */
class GoogleTokenValidationException(message: String) : RuntimeException(message)
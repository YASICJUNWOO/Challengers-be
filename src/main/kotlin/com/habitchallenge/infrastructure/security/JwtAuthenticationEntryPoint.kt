package com.habitchallenge.infrastructure.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerMapping

@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private lateinit var handlerMapping: HandlerMapping

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.contentType = "${MediaType.APPLICATION_JSON_VALUE}; charset=UTF-8"
        response.characterEncoding = "UTF-8"

        // 요청된 경로에 대한 핸들러가 존재하는지 확인
        val handler = try {
            handlerMapping.getHandler(request)
        } catch (e: Exception) {
            null
        }

        if (handler == null) {
            // 핸들러가 없으면 404 Not Found 반환
            response.status = HttpServletResponse.SC_NOT_FOUND
            val body = """
                {
                    "status": 404,
                    "error": "Not Found",
                    "message": "요청한 리소스를 찾을 수 없습니다.",
                    "path": "${request.requestURI}"
                }
            """.trimIndent()
            response.writer.write(body)
        } else {
            // 핸들러가 있으면 401 Unauthorized 반환
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            val body = """
                {
                    "status": 401,
                    "error": "Unauthorized",
                    "message": "인증이 필요합니다.",
                    "path": "${request.requestURI}"
                }
            """.trimIndent()
            response.writer.write(body)
        }

        response.writer.flush()
    }
}
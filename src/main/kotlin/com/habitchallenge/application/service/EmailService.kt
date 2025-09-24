package com.habitchallenge.application.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EmailService {

    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    /**
     * 비밀번호 재설정 인증코드 이메일 발송
     * TODO: 실제 이메일 서비스 연동 (SendGrid, AWS SES 등)
     */
    fun sendPasswordResetCode(email: String, nickname: String, code: String) {
        // 현재는 로그로만 출력 (개발용)
        logger.info("=== 비밀번호 재설정 인증코드 발송 ===")
        logger.info("수신자: $email")
        logger.info("닉네임: $nickname")
        logger.info("인증코드: $code")
        logger.info("내용: 안녕하세요 $nickname 님, 비밀번호 재설정 인증코드는 [$code] 입니다. 10분 내에 입력해주세요.")
        logger.info("=======================================")

        // TODO: 실제 이메일 발송 로직
        // mailSender.send(createResetCodeEmail(email, nickname, code))
    }

    /**
     * 임시 비밀번호 이메일 발송
     * TODO: 실제 이메일 서비스 연동 (SendGrid, AWS SES 등)
     */
    fun sendTemporaryPassword(email: String, nickname: String, temporaryPassword: String) {
        // 현재는 로그로만 출력 (개발용)
        logger.info("=== 임시 비밀번호 발송 ===")
        logger.info("수신자: $email")
        logger.info("닉네임: $nickname")
        logger.info("임시 비밀번호: $temporaryPassword")
        logger.info("내용: 안녕하세요 $nickname 님, 임시 비밀번호는 [$temporaryPassword] 입니다. 로그인 후 반드시 새 비밀번호로 변경해주세요.")
        logger.info("========================")

        // TODO: 실제 이메일 발송 로직
        // mailSender.send(createTemporaryPasswordEmail(email, nickname, temporaryPassword))
    }
}
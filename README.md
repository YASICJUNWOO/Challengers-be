# 🏆 Challenge MVP Backend

**습관 및 목표 관리 SNS 플랫폼의 백엔드 API 서비스**

*A REST API service for habit and goal management social platform*

---

## 📋 개요

Challenge MVP Backend는 사용자들이 함께 습관을 형성하고 목표를 달성할 수 있도록 돕는 소셜 플랫폼의 백엔드 서비스입니다. Spring Boot와 Kotlin을 기반으로 개발되었으며, JWT 인증, 챌린지 그룹 관리, 실시간 알림 시스템 등을 제공합니다.

## ✨ 주요 기능

### 🔐 사용자 관리
- **JWT 기반 인증**: 회원가입, 로그인, 토큰 기반 인증
- **역할별 권한 관리**: 리더(LEADER)와 멤버(MEMBER) 역할 구분
- **프로필 관리**: 사용자 정보 조회 및 수정

### 🎯 챌린지 그룹 관리
- **챌린지 생성 및 관리**: 공개/비공개 챌린지 생성
- **초대 코드 시스템**: 비공개 챌린지 참여를 위한 초대 코드
- **승인제 참여 시스템**: 신청 → 승인/반려 → 참여 워크플로우
- **카테고리별 필터링**: 다양한 챌린지 카테고리 지원

### 📝 활동 인증 시스템
- **인증 업로드**: 텍스트 및 이미지 기반 활동 인증
- **승인 워크플로우**: 리더의 인증 승인/반려 시스템
- **상태별 조회**: 대기중, 승인됨, 반려됨 상태 관리

### 🔔 실시간 알림 시스템
- **다양한 알림 타입**: 챌린지 승인/반려, 그룹 참여, 신청 결과 등
- **자동 알림 스케줄링**: 일일 리마인드, 승인 요약 알림
- **읽음/안읽음 상태 관리**: 개별 및 일괄 읽음 처리

### 📊 통계 및 분석
- **개인 연속 참여 기록**: Streak 계산 및 관리
- **팀 달성률 계산**: 챌린지별 참여율 및 완료율 통계
- **참여자별 통계**: 멤버별 상세 활동 분석

## 🛠 기술 스택

### Backend
- **Language**: Kotlin + Java 21
- **Framework**: Spring Boot 3.2.0
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA + Hibernate
- **Build Tool**: Gradle 8.14.1
- **Test**: JUnit5 + MockK

### Database
- **Development**: H2 In-Memory Database
- **Production**: PostgreSQL

### Documentation
- **API Docs**: Swagger UI (SpringDoc OpenAPI)
- **Specification**: API_SPEC.md, API_PROTOCOL.md, ENTITY_SPEC.md

## 🚀 시작하기

### 전제 조건
- Java 21 이상
- Gradle 8.14.1 이상

### 개발 환경 실행

1. **저장소 클론**
   ```bash
   git clone <repository-url>
   cd challengers-be
   ```

2. **애플리케이션 실행**
   ```bash
   # 개발 모드 (H2 데이터베이스)
   gradle bootRun

   # 특정 포트에서 실행
   SERVER_PORT=8889 gradle bootRun
   ```

3. **빌드 및 테스트**
   ```bash
   # 전체 빌드
   gradle clean build

   # 테스트 실행
   gradle test

   # 테스트 제외 빌드
   gradle clean build -x test
   ```

### 접속 정보
- **API Server**: `http://localhost:8080`
- **H2 Console**: `http://localhost:8080/h2-console`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs**: `http://localhost:8080/v3/api-docs`

### 기본 데모 계정
- **Login ID**: `demo`
- **Password**: `password`

## 📖 API 문서

### Swagger UI
개발 서버 실행 후 다음 URL에서 실시간 API 문서를 확인할 수 있습니다:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

### 주요 API 엔드포인트

#### 인증 관련
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/signin` - 로그인
- `POST /api/auth/logout` - 로그아웃

#### 챌린지 관리
- `GET /api/challenges` - 챌린지 목록 조회
- `POST /api/challenges` - 챌린지 생성
- `GET /api/challenges/{id}` - 챌린지 상세 조회
- `PUT /api/challenges/{id}` - 챌린지 수정

#### 참여 및 신청
- `POST /api/challenges/{id}/apply` - 챌린지 참여 신청
- `POST /api/challenges/join-by-code` - 초대코드로 참여
- `GET /api/challenges/{id}/applications` - 신청 목록 조회

#### 알림 시스템
- `GET /api/notifications` - 알림 목록 조회
- `PUT /api/notifications/{id}/read` - 알림 읽음 처리
- `PUT /api/notifications/read-all` - 모든 알림 읽음 처리

더 자세한 API 스펙은 프로젝트 내 `API_PROTOCOL.md` 파일을 참조하세요.

## 🏗 프로젝트 구조

```
src/main/kotlin/com/habitchallenge/
├── domain/                 # 도메인 레이어
│   ├── challenge/         # 챌린지 엔티티 및 저장소
│   ├── user/             # 사용자 엔티티 및 저장소
│   └── notification/     # 알림 엔티티 및 저장소
├── application/           # 애플리케이션 레이어
│   └── service/          # 비즈니스 로직 서비스
├── infrastructure/        # 인프라 레이어
│   ├── config/           # 설정 (Security, JPA 등)
│   └── security/         # JWT 인증 처리
└── presentation/          # 프레젠테이션 레이어
    ├── controller/       # REST 컨트롤러
    └── dto/             # 요청/응답 DTO
```

### 아키텍처 원칙
- **DDD-lite 구조**: 도메인 중심 설계
- **Clean Architecture**: 계층 간 의존성 역전
- **SOLID 원칙**: 객체지향 설계 원칙 준수

## 🔧 개발 가이드

### 스펙 문서 시스템
이 프로젝트는 버전 관리되는 스펙 문서 시스템을 사용합니다:

- **`CLAUDE.md`**: 프로젝트 전체 가이드라인 및 변경 이력
- **`ENTITY_SPEC.md`**: 데이터베이스 엔티티 스펙
- **`API_SPEC.md`**: REST API 엔드포인트 스펙
- **`API_PROTOCOL.md`**: 백엔드 실제 구현 API 프로토콜

### 변경 사항 적용 순서
1. `CLAUDE.md` 업데이트 (변경 사항 및 Change Log)
2. 관련 스펙 문서 업데이트 (필요시)
3. 실제 코드 구현
4. `API_PROTOCOL.md` 업데이트 (필수)
5. 통합 테스트 및 검증

### 코딩 규칙
- **RESTful API**: 표준 HTTP 메서드와 상태 코드 사용
- **JWT 인증**: Stateless 토큰 기반 인증
- **페이징 지원**: 모든 목록 API는 페이징 처리
- **일관된 에러 처리**: 표준화된 에러 응답 형식

## 🔒 보안 설정

### JWT 설정
- **알고리즘**: HS512
- **만료 시간**: 24시간
- **헤더**: `Authorization: Bearer <token>`

### 엔드포인트 보안
- **공개**: `/api/auth/**`, `/api/challenges` (GET)
- **보호됨**: 기타 모든 엔드포인트는 인증 필요
- **역할 기반**: 챌린지 관리는 리더 권한 필요

## 🤝 기여하기

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### 개발 환경 설정
- JDK 21 이상 설치
- IntelliJ IDEA 또는 기타 Kotlin 지원 IDE 사용 권장
- 코드 스타일은 프로젝트 내 `.editorconfig` 설정 따름

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

## 📞 문의

프로젝트에 대한 문의사항이나 버그 리포트는 GitHub Issues를 통해 제출해 주세요.

---

**Challenge MVP Backend** - 함께 성장하는 습관 형성 플랫폼 🚀
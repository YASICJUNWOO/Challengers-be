# Challenge MVP Backend Project

## Overview
This is the **Challenge MVP Backend** - a Spring Boot REST API service for habit and goal management SNS platform. The backend provides user authentication, challenge group management, and activity logging with approval workflows.

## Tech Stack
- **Language**: Kotlin + Java
- **Framework**: Spring Boot 3.2.0
- **Database**: H2 (development), PostgreSQL (production)
- **ORM**: Spring Data JPA + Hibernate
- **Security**: Spring Security + JWT
- **Build Tool**: Gradle 8.14.1
- **Test**: JUnit5 + MockK
- **Java Version**: 21

## Architecture
- **Domain Layer**: `/domain` - Entities and repositories
- **Application Layer**: `/application/service` - Business logic services
- **Infrastructure Layer**: `/infrastructure` - Security, config, external integrations
- **Presentation Layer**: `/presentation` - REST controllers and DTOs
- **DDD-lite Structure**: Clean separation of concerns with dependency inversion

## Development Guidelines

### Specification Management (Single Source of Truth)
이 프로젝트는 **버전 관리되는 스펙 문서 시스템**을 사용합니다:

#### 1. 스펙 문서 파일들
- **`ENTITY_SPEC.md`**: 데이터베이스 엔티티 스펙 (버전 관리)
- **`API_SPEC.md`**: REST API 엔드포인트 스펙 (버전 관리)
- **`API_PROTOCOL.md`**: 백엔드 실제 구현 API 프로토콜 (버전 관리)
- **`CLAUDE.md`**: 프로젝트 전체 가이드라인 및 변경 이력

#### 2. 변경 사항 적용 순서 (필수)
새로운 기능 추가나 수정 시 반드시 다음 순서를 따라야 함:

```
1. CLAUDE.md 업데이트 (이 파일) - 변경 사항 및 Change Log 작성
2. ENTITY_SPEC.md 업데이트 (필요시) - 엔티티 변경 사항
3. API_SPEC.md 업데이트 (필요시) - API 변경 사항
4. 실제 코드 구현 (엔티티 → 서비스 → 컨트롤러 → 테스트)
5. API_PROTOCOL.md 업데이트 (필수) - 백엔드 실제 구현 반영
6. 통합 테스트 및 검증
```

#### 3. API 관리 규칙 (필수)
**중요**: API/Request/Response 변경 발생 시 다음 문서들을 반드시 최신화해야 함:
- **`API_PROTOCOL.md`**: 백엔드 실제 구현 변경사항 즉시 반영
- **`API_SPEC.md`**: 계획된 API 스펙 변경사항 반영 (필요시)

API 불일치로 인한 프론트엔드-백엔드 호환성 문제를 방지하기 위해 모든 API 변경사항은 문서화 필수.

#### 4. 버전 관리 규칙
- **Major Version** (x.0.0): 호환성이 깨지는 변경 (엔티티 구조 대폭 변경, API 스펙 변경)
- **Minor Version** (x.y.0): 새로운 기능 추가 (새 엔티티, 새 API 엔드포인트)
- **Patch Version** (x.y.z): 버그 수정, 성능 개선

#### 5. 스펙 문서 업데이트 규칙
각 스펙 문서는 다음 정보를 반드시 포함해야 함:
- **Version**: 현재 버전 번호
- **Last Updated**: 최종 수정 날짜
- **Status**: Active/Deprecated/Draft
- **Change Log**: 버전별 변경 사항 상세 기록

### API Design Principles
1. **RESTful 설계**: 표준 HTTP 메서드와 상태코드 사용
2. **Frontend 호환성**: 프론트엔드 Mock API 스펙과 완벽 호환
3. **JWT 인증**: Stateless 토큰 기반 인증 시스템
4. **페이징 지원**: 모든 목록 API는 페이징 처리
5. **에러 처리**: 일관된 에러 응답 형식

### Entity Design Principles
1. **JPA 표준 준수**: Hibernate 최적화된 엔티티 설계
2. **Audit 지원**: BaseEntity를 통한 생성/수정 시간 자동 관리
3. **관계 매핑**: Lazy Loading 기본, 필요시에만 Eager
4. **검증 규칙**: Bean Validation 애노테이션 적극 활용
5. **불변성**: Entity 필드는 가능한 val (immutable) 사용

## Environment Configuration

### Development (기본)
- **Database**: H2 in-memory
- **Server Port**: 8080
- **H2 Console**: http://localhost:8080/h2-console
- **Profile**: default

### Production
- **Database**: PostgreSQL
- **Profile**: prod
- **Environment Variables**:
  - `DB_USERNAME`: Database username
  - `DB_PASSWORD`: Database password
  - `JWT_SECRET`: JWT signing secret

## Commands
- **Development**: `gradle bootRun`
- **Build**: `gradle clean build`
- **Test**: `gradle test`
- **Build without tests**: `gradle clean build -x test`

## Key Features Implemented

### User Management
- JWT 기반 회원가입/로그인
- 역할별 권한 관리 (LEADER/MEMBER)
- 프로필 관리

### Challenge Groups
- 챌린지 그룹 생성/조회
- 승인제 그룹 참여 시스템 (신청/승인/반려)
- 리더 권한 관리
- 카테고리별 필터링

### Activity Logging
- 인증 업로드 (텍스트/이미지)
- 승인/반려 워크플로우
- 상태별 조회 (pending/approved/rejected)

### Notification System
- 실시간 알림 시스템 (챌린지 승인/반려, 그룹 참여, 참여 신청 결과 등)
- 알림 타입별 분류 (challenge_approved, challenge_rejected, group_joined, group_started, group_ended, application_approved, application_rejected, system)
- 읽음/안읽음 상태 관리
- 관련 URL 및 액션 정보 포함

### Statistics & Analytics
- 개인 연속 참여 기록 (Streak)
- 팀 달성률 계산
- 참여자별 완료율 통계

## Database Schema

### Core Tables
- `users`: 사용자 정보 및 인증
- `challenge_groups`: 챌린지 그룹 메타데이터
- `participations`: 사용자-그룹 참여 관계
- `group_applications`: 그룹 참여 신청 데이터
- `challenge_logs`: 활동 인증 로그
- `challenge_tags`: 그룹 태그 (ElementCollection)
- `notifications`: 사용자 알림 데이터

### Relationships
```
users (1) ←→ (N) participations (N) ←→ (1) challenge_groups
users (1) ←→ (N) group_applications (N) ←→ (1) challenge_groups
users (1) ←→ (N) challenge_logs (N) ←→ (1) challenge_groups
users (1) ←→ (N) notifications
challenge_groups (1) ←→ (1) users [leader_id]
```

## Security Configuration

### JWT Configuration
- **Algorithm**: HS512
- **Expiration**: 24 hours
- **Secret**: Configurable via application.yml
- **Header**: `Authorization: Bearer <token>`

### Endpoint Security
- **Public**: `/api/auth/**`, `/api/groups` (GET only)
- **Protected**: All other endpoints require authentication
- **Role-based**: Group management requires LEADER role

## QA Checklist

### Functionality Tests
- [ ] 회원가입/로그인 플로우 정상 작동
- [ ] 챌린지 그룹 CRUD 작업 검증
- [ ] 참여/탈퇴 비즈니스 로직 확인
- [ ] 인증 업로드 및 승인 워크플로우 테스트
- [ ] JWT 토큰 생성 및 검증 확인
- [ ] 페이징 및 필터링 기능 검증

### Security Tests
- [ ] 비인증 사용자 접근 차단 확인
- [ ] JWT 토큰 만료 처리 검증
- [ ] 권한별 접근 제어 확인
- [ ] SQL Injection 방어 확인

### Performance Tests
- [ ] 대량 데이터 조회 성능 검증
- [ ] N+1 쿼리 문제 해결 확인
- [ ] 인덱스 효율성 검증

## Change Log

### v1.0.0 (2025-09-15): Initial Implementation ✅
- ✅ **Project Setup**: Spring Boot 3.2.0 + Kotlin + Gradle 구성
- ✅ **Entity Design**: User, ChallengeGroup, Participation, ChallengeLog 엔티티 구현
- ✅ **JWT Security**: Spring Security + JWT 인증 시스템 구축
- ✅ **REST API**: 프론트엔드 호환 REST API 엔드포인트 구현
- ✅ **Business Logic**: 챌린지 참여, 인증 승인/반려 로직 구현
- ✅ **Statistics Service**: Streak 계산, 팀 달성률 집계 기능
- ✅ **Database Schema**: H2/PostgreSQL 호환 스키마 설계
- ✅ **Circular Dependency Fix**: UserDetailsService 분리로 순환 참조 해결
- ✅ **Application Startup**: 정상 부팅 및 API 서비스 제공 확인

### v1.0.1 (2025-09-15): Specification System Implementation ✅
- ✅ **Specification Management**: ENTITY_SPEC.md, API_SPEC.md 문서화 시스템 구축
- ✅ **Version Control Rules**: 스펙 문서 버전 관리 및 변경 프로세스 정의
- ✅ **Frontend Compatibility**: Mock API 스펙과 완벽 호환되는 응답 형식 정의
- ✅ **Change Management Process**: 변경 사항 적용 순서 및 규칙 수립

### v1.0.2 (2025-09-15): Character Encoding Fix ✅
- ✅ **UTF-8 Encoding Fix**: JwtAuthenticationEntryPoint에서 한글 문자 인코딩 문제 해결
- ✅ **Response Headers**: Content-Type에 charset=UTF-8 명시적 설정
- ✅ **Character Encoding**: response.characterEncoding = "UTF-8" 추가
- ✅ **Writer Usage**: outputStream 대신 writer 사용으로 문자 인코딩 안정성 향상

### v1.1.0 (2025-09-16): Notification System Implementation ✅
- ✅ **Notification Entity**: 알림 데이터 모델 및 JPA 엔티티 구현 (Notification.kt)
- ✅ **Notification Repository**: 알림 데이터 접근 레이어 구현 (NotificationRepository.kt)
- ✅ **Notification Service**: 알림 생성, 조회, 읽음 처리 비즈니스 로직 (NotificationService.kt)
- ✅ **Notification API**: REST 엔드포인트 (/api/notifications) 구현 (NotificationController.kt)
- ✅ **Notification Types**: 챌린지 승인/반려, 그룹 참여 등 6가지 알림 타입 지원
- ✅ **Read Status Management**: 개별/일괄 읽음 처리 기능 완료
- ✅ **DTOs & Validation**: 요청/응답 DTO 및 입력 검증 규칙 구현 (NotificationDto.kt)
- ✅ **Database Schema**: 인덱스 최적화된 notifications 테이블 생성 및 외래키 관계 설정
- ✅ **API Testing**: 모든 엔드포인트 (CRUD + 읽음처리) 동작 검증 완료

### v1.1.1 (2025-09-16): 404 Error Handling Fix ✅
- ✅ **JwtAuthenticationEntryPoint 개선**: HandlerMapping을 활용한 404/401 에러 구분 처리 구현
- ✅ **존재하지 않는 URL**: 404 Not Found 응답으로 변경 (기존: 401 Unauthorized)
- ✅ **인증 실패 URL**: 여전히 401 Unauthorized 응답 유지
- ✅ **HandlerMapping 의존성**: @Qualifier 애노테이션으로 requestMappingHandlerMapping 지정
- ✅ **에러 메시지 한글화**: "요청한 리소스를 찾을 수 없습니다." 메시지 추가
- ✅ **API 호환성**: 기존 인증 로직과 에러 응답 형식 유지

### v1.2.0 (2025-09-16): Frontend-Backend API Alignment ✅
- ✅ **Frontend API 분석**: 프론트엔드에서 사용하는 모든 API 호출 패턴 분석 완료
- ✅ **Backend API 구현 분석**: 백엔드 컨트롤러별 구현 현황 전면 분석 완료
- ✅ **누락 API 식별**: 프론트엔드 요구사항과 백엔드 구현 간 갭 분석 완료
- ✅ **AuthController 확장**: POST /api/auth/logout 엔드포인트 추가 구현
- ✅ **UserController 확장**: GET /api/users/me 엔드포인트 추가 구현 (@AuthenticationPrincipal 활용)
- ✅ **GroupController 확장**: PUT /api/groups/{id} 그룹 수정 엔드포인트 추가 구현
- ✅ **GroupService 확장**: updateGroup 메소드 추가 (부분 업데이트 지원, 리더 권한 검증)
- ✅ **DTO 확장**: UpdateGroupRequest DTO 추가 (선택적 필드 업데이트 지원)
- ✅ **API_SPEC.md 업데이트**: 새로 추가된 3개 API 문서화 및 Base URL 수정 (8080→8888)
- ✅ **버전 관리**: v1.2.0으로 업그레이드 및 변경 이력 정리

### v1.2.1 (2025-09-17): Authentication & Group Join API Updates ✅
- ✅ **인증 시스템 변경**: 로그인 인증 필드를 email에서 loginId로 변경
- ✅ **User Entity 변경**: email 필드를 loginId로 교체 (데모 계정: demo@demo.com → demo)
- ✅ **로그인 API 수정**: POST /api/auth/signin 요청 형식 변경 ({ email, password } → { id, password })
- ✅ **그룹 참여 API 분리**: 전용 엔드포인트 POST /api/groups/{id}/join 추가
- ✅ **API 스펙 업데이트**: 인증 및 그룹 참여 API 변경사항 문서화
- ✅ **하위 호환성**: 기존 PUT 방식 fallback 지원 유지

### v1.3.0 (2025-09-17): Challenge Application Approval System ✅
- ✅ **ChallengeApplication Entity**: 챌린지 참여 신청 데이터 모델 구현 (승인제 워크플로우)
- ✅ **Approval Workflow API**: 4개 신규 엔드포인트 구현
  - POST /api/challenges/{id}/apply - 참여 신청 (사유 포함)
  - GET /api/challenges/{id}/applications - 신청 목록 조회 (리더 전용)
  - PUT /api/challenges/{id}/applications/{applicationId}/status - 승인/반려 처리
  - GET /api/users/me/applications - 사용자 신청 현황 조회
- ✅ **Notification System 확장**: application_approved, application_rejected 알림 타입 추가
- ✅ **Auto Challenge Join**: 신청 승인 시 자동 챌린지 멤버 추가 로직
- ✅ **Legacy API Support**: POST /api/challenges/{id}/join deprecated 처리 (하위 호환 유지)
- ✅ **Status Management**: pending → approved/rejected 상태 전환 관리

### v1.4.0 (2025-09-17): Enhanced Notification System & Domain Unification ✅
- ✅ **Enhanced Notification Types**: 4개 신규 알림 타입 추가
  - NEW_CHALLENGE_LOG - 새로운 인증 업로드 발생 (매니저 대상)
  - NEW_APPLICATION - 참여 신청 발생 (매니저 대상)
  - DAILY_REMINDER - 오늘 인증 미제출 리마인드 (참여자 대상)
  - DAILY_APPROVAL_SUMMARY - 승인 대기 인증 요약 (매니저 대상)
- ✅ **Notification Scheduler Service**: 자동 알림 스케줄링 시스템 구현
  - 매일 저녁 8시: 인증 미제출 리마인드 알림
  - 매일 오전 9시: 승인 대기 인증 요약 알림
  - 매일 오전 10시: 챌린지 시작 알림
  - 매일 오후 11시: 챌린지 종료 알림
- ✅ **Domain Term Unification**: 전역적으로 "group" → "challenge" 용어 통일
  - ChallengeGroup → Challenge 엔티티 통합
  - GroupService → ChallengeService 서비스 리네이밍
  - 모든 API 엔드포인트 및 DTO에서 일관된 "challenge" 용어 사용
  - 데이터베이스 컬럼명 및 관계 매핑 업데이트 (group_id → challenge_id)
- ✅ **Automatic Notification Triggers**: 비즈니스 로직에 자동 알림 전송 통합
  - 인증 업로드 시 리더에게 자동 알림
  - 인증 승인/반려 시 참여자에게 자동 알림
  - 참여 신청 시 리더에게 자동 알림
  - 신청 승인/반려 시 신청자에게 자동 알림

### v1.5.2 (2025-09-18): Public Access for Invite Code API ✅
- ✅ **Security Configuration Update**: SecurityConfig에서 `/api/challenges/invite/**` 경로를 permitAll()로 설정
- ✅ **Controller Logic Simplification**: getChallengeByInviteCode 메소드에서 인증 요구사항 및 권한 검사 제거
- ✅ **Public Access Design**: 초대코드를 가진 모든 사용자가 비공개 챌린지 정보 조회 가능
- ✅ **Privacy Protection Maintained**: 일반 챌린지 목록에서는 여전히 비공개 챌린지를 리더/참여자만 조회 가능
- ✅ **API Specification Update**: API_SPEC.md v1.3.2 업데이트 (인증 요구사항 제거, 에러 처리 단순화)
- ✅ **User Experience Improvement**: 초대코드 기반 참여 플로우에서 사전 로그인 불필요

### v1.5.1 (2025-09-18): Invite Code API Documentation & Frontend Flow Alignment ✅
- ✅ **API Specification Update**: API_SPEC.md v1.3.1 업데이트 완료
- ✅ **Invite Code Lookup API**: GET /api/challenges/invite/{inviteCode} 엔드포인트 문서화
- ✅ **Frontend Flow Alignment**: 프론트엔드 비공개 챌린지 참여 프로세스와 맞춘 API 스펙 정의
- ✅ **Error Handling Documentation**: 404 (유효하지 않은 초대코드), 403 (접근 권한 없음) 에러 처리 규칙 정의
- ✅ **Usage Context**: 초대코드 입력 시 챌린지 정보 조회를 위한 API 사용 목적 명시
- ✅ **Authentication Requirements**: Authorization Bearer 토큰 요구사항 문서화
- ✅ **Parameter Specification**: 8자리 대문자+숫자 초대코드 파라미터 규격 정의

### v1.5.0 (2025-09-18): Enhanced Challenge Creation & Private Challenge System ✅
- ✅ **Leader Role Management**: 챌린지 생성 시 리더 역할 기반 멤버 관리 시스템
  - `leaderRole: 'participant' | 'manager'` 필드 추가
  - participant: 리더가 멤버로 자동 참여 (members 배열에 포함)
  - manager: 리더는 관리만 담당 (멤버 목록에 미포함)
- ✅ **Private Challenge System**: 비공개 챌린지 및 초대 코드 시스템
  - `isPrivate: boolean` 필드 추가 (기본값: false)
  - `inviteCode: string` 자동 생성 (비공개일 경우 대문자+숫자 8자리)
  - 비공개 챌린지는 일반 목록에서 제외
  - POST /api/challenges/join-by-code 엔드포인트 추가
- ✅ **Entity & Repository Updates**: Challenge 엔티티 및 ChallengeRepository 확장
  - LeaderRole enum 추가 (PARTICIPANT, MANAGER)
  - isPrivate, inviteCode, leaderRole 필드 추가
  - 비공개 챌린지 필터링을 위한 Repository 메소드 추가
- ✅ **Service Layer Enhancement**: ChallengeService 로직 개선
  - 초대 코드 자동 생성 (generateInviteCode)
  - 리더 역할에 따른 자동 참여 로직
  - 비공개 챌린지 전용 참여 메소드 (joinChallengeByInviteCode)
  - 모든 조회 API에서 비공개 챌린지 자동 제외
- ✅ **API & DTO Updates**: 컨트롤러 및 DTO 확장
  - CreateChallengeRequest에 isPrivate, leaderRole 필드 추가
  - ChallengeResponse에 isPrivate, inviteCode, leaderRole 필드 추가
  - JoinChallengeByInviteCodeRequest DTO 추가
  - 입력 검증 및 오류 메시지 한글화

### v1.6.1 (2025-09-19): 회원가입 API 이메일 필드 지원 ✅
- ✅ **SignupRequest DTO 확장**: 이메일 필드 추가 및 검증 규칙 적용
  - `email` 필드 추가 (유효한 이메일 형식 검증)
  - `loginId` 검증 규칙 수정 (2-20자, 영문/숫자/언더스코어/하이픈만 허용)
- ✅ **User Entity 업데이트**: 이메일 필드 추가 및 Unique 제약 조건 설정
- ✅ **UserService 확장**: 이메일 중복 검사 및 사용자 생성 로직 개선
- ✅ **AuthController 수정**: 회원가입 엔드포인트에서 이메일 필드 처리
- ✅ **UserRepository 확장**: `existsByEmail` 메소드 추가
- ✅ **API_PROTOCOL.md 업데이트**: 회원가입 API 스펙 최신화
- ✅ **프론트엔드 호환성**: 프론트엔드 요구사항에 맞춘 API 구조 완성

### v1.6.0 (2025-09-19): Backend API Protocol Documentation System ✅
- ✅ **API_PROTOCOL.md 생성**: 백엔드 실제 구현 기준 API 문서화 시스템 구축
  - 5개 컨트롤러, 27개 엔드포인트 완전 문서화
  - 프론트엔드와 동일한 Headers/Request/Response 통합 구조 적용
  - Kotlin DTO 클래스 기반 정확한 타입 정의
- ✅ **API 관리 규칙 추가**: CLAUDE.md에 API 변경 시 필수 문서화 규칙 추가
  - API/Request/Response 변경 시 API_PROTOCOL.md 즉시 업데이트 필수
  - 프론트엔드-백엔드 API 호환성 문제 예방 시스템
- ✅ **백엔드 전용 기능 문서화**: 프론트엔드에 없는 추가 기능들 포함
  - 추가 사용자 관리 엔드포인트 (GET /api/users, /api/users/me/challenges 등)
  - 다양한 챌린지 참여 방식 (초대코드, 사용자ID 등)
  - 확장된 알림 타입 (new_challenge_log, daily_reminder 등)
  - 읽지 않은 알림 개수 조회
- ✅ **Spring Data 표준 반영**: Page<T> 페이지네이션 구조 정확히 문서화
- ✅ **검증 규칙 포함**: @Valid 애노테이션 기반 입력 검증 규칙 완전 반영

## Deployment Notes
- **Docker Support**: 향후 컨테이너화 예정
- **CI/CD**: GitHub Actions 파이프라인 구축 예정
- **Monitoring**: 애플리케이션 모니터링 및 로깅 시스템 연동 예정
- **Load Balancing**: 멀티 인스턴스 배포를 위한 로드 밸런서 설정 예정

---

## Important Instructions for AI Assistant

### 모든 변경 사항 적용 시 필수 규칙:

1. **이 CLAUDE.md 파일을 가장 먼저 업데이트**
   - Change Log 섹션에 새로운 변경 사항 추가
   - 관련 섹션 (Overview, Tech Stack 등) 필요시 수정

2. **스펙 문서 업데이트 (필요시)**
   - ENTITY_SPEC.md: 엔티티 구조 변경시
   - API_SPEC.md: API 엔드포인트 변경시
   - 버전 번호 증가 및 Change Log 업데이트

3. **실제 코드 구현**
   - 업데이트된 스펙 문서를 기준으로 코드 작성
   - 스펙과 코드 간 일치성 유지

4. **API_PROTOCOL.md 업데이트 (필수)**
   - 백엔드 실제 구현사항을 API_PROTOCOL.md에 즉시 반영
   - 프론트엔드-백엔드 API 호환성 확보

5. **검증 및 테스트**
   - 변경사항에 대한 단위/통합 테스트 수행
   - API 호환성 검증

이 시스템을 통해 프로젝트의 모든 변경사항이 체계적으로 관리되고 추적됩니다.
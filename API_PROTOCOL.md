# API_PROTOCOL.md

백엔드 실제 구현 API 프로토콜 정의서

---

## 인증 (Authentication)

### POST /api/auth/signin
- **Description**: 사용자 로그인
- **Headers**
  - Content-Type: application/json
- **Request Body**
  ```typescript
  {
    id: string;        // 로그인 ID
    password: string;  // 패스워드
  }
  ```
- **Response Body**
  ```typescript
  {
    token: string;     // JWT 토큰
    user: {            // 사용자 정보
      id: string;
      name: string;
      loginId: string;
      avatar?: string;
      role: 'leader' | 'member';
      createdAt: string; // ISO 8601
    };
  }
  ```

### POST /api/auth/google
- **Description**: Google OAuth 로그인
- **Headers**
  - Content-Type: application/json
- **Request Body**
  ```typescript
  {
    token: string;  // Google OAuth id_token
  }
  ```
- **Response Body**
  ```typescript
  {
    token: string;     // JWT 토큰
    user: {            // 사용자 정보
      id: string;
      name: string;
      loginId: string;
      avatar?: string;
      role: 'leader' | 'member';
      createdAt: string; // ISO 8601
    };
  }
  ```
- **Error Responses**
  - **401 Unauthorized**: Google 토큰 검증 실패
    ```typescript
    {
      error: "토큰 검증 실패";
      message: string;
    }
    ```
  - **400 Bad Request**: 이미 존재하는 이메일
    ```typescript
    {
      error: "잘못된 요청";
      message: string;
    }
    ```
  - **500 Internal Server Error**: 서버 오류
    ```typescript
    {
      error: "서버 오류";
      message: "Google 로그인 처리 중 오류가 발생했습니다.";
    }
    ```

### POST /api/auth/signup
- **Description**: 사용자 회원가입
- **Headers**
  - Content-Type: application/json
- **Request Body**
  ```typescript
  {
    email: string;                    // 이메일 (유효한 이메일 형식)
    loginId: string;                  // 로그인 ID (2-20자, 영문/숫자/언더스코어/하이픈만 허용)
    password: string;                 // 패스워드 (8-50자)
    nickname: string;                 // 닉네임 (2-20자)
    role: 'LEADER' | 'MEMBER';       // 역할 (기본값: MEMBER)
  }
  ```
- **Response Body**
  ```typescript
  {
    token: string;     // JWT 토큰
    user: {            // 사용자 정보
      id: string;
      name: string;
      loginId: string;
      avatar?: string;
      role: 'leader' | 'member';
      createdAt: string; // ISO 8601
    };
  }
  ```

### POST /api/auth/logout
- **Description**: 사용자 로그아웃
- **Headers**
  - Authorization: Bearer {token}
- **Response Body**
  ```typescript
  {
    message: string;  // "로그아웃되었습니다."
  }
  ```

---

## 사용자 (Users)

### GET /api/users
- **Description**: 사용자 목록 조회 (페이지네이션)
- **Headers**
  - Authorization: Bearer {token}
- **Query Parameters**
  - page?: number (페이지 번호, 기본값: 0)
  - size?: number (페이지 크기, 기본값: 10)
  - role?: string (역할 필터: 'LEADER', 'MEMBER')
- **Response Body**
  ```typescript
  {
    content: User[];          // 사용자 배열
    page: number;             // 현재 페이지
    size: number;             // 페이지 크기
    totalElements: number;    // 전체 항목 수
    totalPages: number;       // 전체 페이지 수
    first: boolean;           // 첫 페이지 여부
    last: boolean;            // 마지막 페이지 여부
    numberOfElements: number; // 현재 페이지 항목 수
    empty: boolean;           // 빈 페이지 여부
  }
  ```

### GET /api/users/{id}
- **Description**: 특정 사용자 정보 조회
- **Headers**
  - Authorization: Bearer {token}
- **Request Params**
  - id: number (사용자 ID)
- **Response Body**
  ```typescript
  {
    id: string;            // 사용자 ID
    name: string;          // 이름
    loginId: string;       // 로그인 ID
    avatar?: string;       // 아바타 URL
    role: 'leader' | 'member';  // 역할
    createdAt: string;     // 생성일 (ISO 8601)
  }
  ```

### GET /api/users/me
- **Description**: 현재 인증된 사용자 정보 조회
- **Headers**
  - Authorization: Bearer {token}
- **Response Body**
  ```typescript
  {
    id: string;            // 사용자 ID
    name: string;          // 이름
    loginId: string;       // 로그인 ID
    avatar?: string;       // 아바타 URL
    role: 'leader' | 'member';  // 역할
    createdAt: string;     // 생성일 (ISO 8601)
  }
  ```

### GET /api/users/me/applications
- **Description**: 현재 사용자의 챌린지 신청 목록 조회
- **Headers**
  - Authorization: Bearer {token}
- **Query Parameters**
  - status?: string (신청 상태 필터: 'PENDING', 'APPROVED', 'REJECTED')
  - page?: number (페이지 번호, 기본값: 0)
  - size?: number (페이지 크기, 기본값: 10)
- **Response Body**
  ```typescript
  {
    content: ChallengeApplication[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
    numberOfElements: number;
    empty: boolean;
  }

  // ChallengeApplication 타입
  interface ChallengeApplication {
    id: string;                    // 신청 ID
    challengeId: string;           // 챌린지 ID
    userId: string;                // 신청자 ID
    reason: string;                // 신청 사유
    status: 'pending' | 'approved' | 'rejected';  // 상태
    createdAt: string;             // 신청일 (ISO 8601)
    reviewedAt?: string;           // 검토일 (ISO 8601)
    rejectionReason?: string;      // 반려 사유
    user: {                        // 신청자 정보
      id: string;
      name: string;
      avatar?: string;
    };
  }
  ```

### GET /api/users/me/challenges
- **Description**: 현재 사용자가 참여한 챌린지 목록 조회
- **Headers**
  - Authorization: Bearer {token}
- **Query Parameters**
  - page?: number (페이지 번호, 기본값: 0)
  - size?: number (페이지 크기, 기본값: 10)
- **Response Body**
  ```typescript
  {
    content: Challenge[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
    numberOfElements: number;
    empty: boolean;
  }
  ```

### GET /api/users/{id}/challenges
- **Description**: 특정 사용자가 참여한 챌린지 목록 조회
- **Headers**
  - Authorization: Bearer {token}
- **Request Params**
  - id: number (사용자 ID)
- **Query Parameters**
  - page?: number (페이지 번호, 기본값: 0)
  - size?: number (페이지 크기, 기본값: 10)
- **Response Body**
  ```typescript
  {
    content: Challenge[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
    numberOfElements: number;
    empty: boolean;
  }
  ```

---

## 챌린지 (Challenges)

### GET /api/challenges
- **Description**: 챌린지 목록 조회 (페이지네이션, 필터링)
- **Headers**
  - Authorization: Bearer {token}
- **Query Parameters**
  - page?: number (페이지 번호, 기본값: 0)
  - size?: number (페이지 크기, 기본값: 10)
  - category?: string (카테고리 필터)
  - status?: string (상태 필터)
  - search?: string (검색어)
- **Response Body**
  ```typescript
  {
    content: Challenge[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
    numberOfElements: number;
    empty: boolean;
  }

  // Challenge 타입
  interface Challenge {
    id: string;                                                              // 챌린지 ID
    name: string;                                                            // 챌린지 이름
    description: string;                                                     // 설명
    category: 'health' | 'study' | 'habit' | 'hobby' | 'social' | 'business'; // 카테고리
    difficulty: 'easy' | 'medium' | 'hard';                                 // 난이도
    duration: number;                                                        // 진행 기간 (일)
    startDate: string;                                                       // 시작일 (YYYY-MM-DD)
    endDate: string;                                                         // 종료일 (YYYY-MM-DD)
    maxMembers: number;                                                      // 최대 멤버 수
    currentMembers: number;                                                  // 현재 멤버 수
    leaderId: string;                                                        // 리더 ID
    leader: User;                                                            // 리더 정보
    members: User[];                                                         // 멤버 목록
    status: 'recruiting' | 'active' | 'completed';                          // 상태
    coverImage?: string;                                                     // 커버 이미지 URL
    reward?: string;                                                         // 보상
    tags: string[];                                                          // 태그 목록
    createdAt: string;                                                       // 생성일 (ISO 8601)
    isPrivate?: boolean;                                                     // 비공개 여부
    inviteCode?: string;                                                     // 초대 코드
    leaderRole?: 'participant' | 'manager';                                 // 리더 역할
  }
  ```

### GET /api/challenges/{id}
- **Description**: 특정 챌린지 상세 조회
- **Headers**
  - Authorization: Bearer {token}
- **Request Params**
  - id: number (챌린지 ID)
- **Response Body**
  ```typescript
  Challenge // 위의 Challenge 인터페이스와 동일
  ```

### GET /api/challenges/invite/{inviteCode}
- **Description**: 초대 코드로 비공개 챌린지 조회 (인증 불필요)
- **Request Params**
  - inviteCode: string (8자리 초대 코드)
- **Response Body**
  ```typescript
  Challenge // 위의 Challenge 인터페이스와 동일
  ```

### POST /api/challenges
- **Description**: 새 챌린지 생성
- **Headers**
  - Authorization: Bearer {token}
  - Content-Type: application/json
- **Request Body**
  ```typescript
  {
    name: string;                                                              // 챌린지 이름 (2-100자)
    description: string;                                                       // 설명 (10-1000자)
    category: 'health' | 'study' | 'habit' | 'hobby' | 'social' | 'business'; // 카테고리
    difficulty: 'easy' | 'medium' | 'hard';                                   // 난이도
    duration: number;                                                          // 진행 기간 (1-365일)
    startDate: string;                                                         // 시작일 (YYYY-MM-DD)
    endDate: string;                                                           // 종료일 (YYYY-MM-DD)
    maxMembers: number;                                                        // 최대 멤버 수 (2-1000)
    coverImage?: string;                                                       // 커버 이미지 URL
    reward?: string;                                                           // 보상 (최대 500자)
    tags?: string[];                                                           // 태그 목록
    isPrivate?: boolean;                                                       // 비공개 여부 (기본값: false)
    leaderRole?: 'participant' | 'manager';                                   // 리더 역할 (기본값: participant)
  }
  ```
- **Response Body**
  ```typescript
  Challenge // 위의 Challenge 인터페이스와 동일
  ```

### PUT /api/challenges/{id}
- **Description**: 챌린지 정보 수정 (리더 전용)
- **Headers**
  - Authorization: Bearer {token}
  - Content-Type: application/json
- **Request Params**
  - id: number (챌린지 ID)
- **Request Body**
  ```typescript
  {
    name?: string;                                                             // 챌린지 이름
    description?: string;                                                      // 설명
    category?: 'health' | 'study' | 'habit' | 'hobby' | 'social' | 'business'; // 카테고리
    difficulty?: 'easy' | 'medium' | 'hard';                                  // 난이도
    duration?: number;                                                         // 진행 기간
    startDate?: string;                                                        // 시작일
    endDate?: string;                                                          // 종료일
    maxMembers?: number;                                                       // 최대 멤버 수
    coverImage?: string;                                                       // 커버 이미지
    reward?: string;                                                           // 보상
    tags?: string[];                                                           // 태그 목록
  }
  ```
- **Response Body**
  ```typescript
  Challenge // 위의 Challenge 인터페이스와 동일
  ```

### DELETE /api/challenges/{id}
- **Description**: 챌린지 삭제 (리더 전용)
- **Headers**
  - Authorization: Bearer {token}
- **Request Params**
  - id: number (챌린지 ID)
- **Response Body**
  ```typescript
  {
    success: boolean;               // 삭제 성공 여부
    message: string;                // 삭제 결과 메시지
    deletedAt: string;              // 삭제 시간 (ISO 8601)
  }
  ```
- **Error Responses**
  - 403 Forbidden: 리더가 아닌 사용자가 삭제 시도
  - 404 Not Found: 존재하지 않는 챌린지 ID

### PUT /api/challenges/{id}/join
- **Description**: 챌린지 참여 (Legacy 엔드포인트)
- **Headers**
  - Authorization: Bearer {token}
  - Content-Type: application/json
- **Request Params**
  - id: number (챌린지 ID)
- **Request Body**
  ```typescript
  {
    joinReason?: string;    // 참여 사유 (최대 500자)
  }
  ```
- **Response Body**
  ```typescript
  Challenge // 위의 Challenge 인터페이스와 동일
  ```

### PUT /api/challenges/{id}/leave
- **Description**: 챌린지 탈퇴
- **Headers**
  - Authorization: Bearer {token}
- **Request Params**
  - id: number (챌린지 ID)
- **Response Body**
  ```typescript
  Challenge // 위의 Challenge 인터페이스와 동일
  ```

### POST /api/challenges/{id}/join
- **Description**: 사용자 ID로 챌린지 참여
- **Headers**
  - Authorization: Bearer {token}
  - Content-Type: application/json
- **Request Params**
  - id: number (챌린지 ID)
- **Request Body**
  ```typescript
  {
    userId: string;         // 사용자 ID
    joinReason?: string;    // 참여 사유 (최대 500자)
  }
  ```
- **Response Body**
  ```typescript
  Challenge // 위의 Challenge 인터페이스와 동일
  ```

### POST /api/challenges/join-by-code
- **Description**: 초대 코드로 챌린지 참여
- **Headers**
  - Authorization: Bearer {token}
  - Content-Type: application/json
- **Request Body**
  ```typescript
  {
    inviteCode: string;     // 초대 코드 (8자리)
    joinReason?: string;    // 참여 사유 (최대 500자)
  }
  ```
- **Response Body**
  ```typescript
  Challenge // 위의 Challenge 인터페이스와 동일
  ```

### POST /api/challenges/{id}/apply
- **Description**: 챌린지 참여 신청
- **Headers**
  - Authorization: Bearer {token}
  - Content-Type: application/json
- **Request Params**
  - id: number (챌린지 ID)
- **Request Body**
  ```typescript
  {
    reason: string;    // 신청 사유 (10-500자)
  }
  ```
- **Response Body**
  ```typescript
  {
    id: string;                    // 신청 ID
    challengeId: string;           // 챌린지 ID
    userId: string;                // 신청자 ID
    reason: string;                // 신청 사유
    status: 'pending' | 'approved' | 'rejected';  // 상태
    createdAt: string;             // 신청일 (ISO 8601)
    reviewedAt?: string;           // 검토일 (ISO 8601)
    rejectionReason?: string;      // 반려 사유
    user: {                        // 신청자 정보
      id: string;
      name: string;
      avatar?: string;
    };
  }
  ```

### GET /api/challenges/{id}/applications
- **Description**: 챌린지 신청 목록 조회 (리더 전용)
- **Headers**
  - Authorization: Bearer {token}
- **Request Params**
  - id: number (챌린지 ID)
- **Query Parameters**
  - status?: string (신청 상태 필터: 'PENDING', 'APPROVED', 'REJECTED')
  - page?: number (페이지 번호, 기본값: 0)
  - size?: number (페이지 크기, 기본값: 10)
- **Response Body**
  ```typescript
  {
    content: ChallengeApplication[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
    numberOfElements: number;
    empty: boolean;
  }
  ```

### PUT /api/challenges/{id}/applications/{applicationId}/status
- **Description**: 챌린지 신청 상태 변경 (승인/반려)
- **Headers**
  - Authorization: Bearer {token}
  - Content-Type: application/json
- **Request Params**
  - id: number (챌린지 ID)
  - applicationId: number (신청 ID)
- **Request Body**
  ```typescript
  {
    status: 'approved' | 'rejected';  // 신청 상태
    rejectionReason?: string;         // 반려 사유 (반려 시 10-500자 필수)
  }
  ```
- **Response Body**
  ```typescript
  {
    id: string;                    // 신청 ID
    challengeId: string;           // 챌린지 ID
    userId: string;                // 신청자 ID
    reason: string;                // 신청 사유
    status: 'pending' | 'approved' | 'rejected';  // 상태
    createdAt: string;             // 신청일 (ISO 8601)
    reviewedAt?: string;           // 검토일 (ISO 8601)
    rejectionReason?: string;      // 반려 사유
    user: {                        // 신청자 정보
      id: string;
      name: string;
      avatar?: string;
    };
  }
  ```

### GET /api/challenges/{id}/members/stats
- **Description**: 챌린지 멤버별 통계 조회 (Streak, 달성률)
- **Headers**
  - Authorization: Bearer {token}
- **Request Params**
  - id: number (챌린지 ID)
- **Response Body**
  ```typescript
  [
    {
      memberId: string;              // 멤버 ID
      streak: number;                // 연속 참여일 수
      achievementRate: number;       // 달성률 (0-100)
      totalSubmissions: number;      // 총 제출 횟수
      approvedSubmissions: number;   // 승인된 제출 횟수
      lastSubmissionDate?: string;   // 마지막 제출일 (ISO 8601)
    }
  ]
  ```

### GET /api/challenges/{id}/participation
- **Description**: 챌린지 참여율 데이터 조회
- **Headers**
  - Authorization: Bearer {token}
- **Request Params**
  - id: number (챌린지 ID)
- **Query Parameters**
  - userId?: number (특정 사용자 필터 - 개인 참여율 조회용)
  - startDate?: string (시작일 필터, YYYY-MM-DD 형식)
  - endDate?: string (종료일 필터, YYYY-MM-DD 형식)
- **Response Body**
  ```typescript
  [
    {
      date: string;                  // 날짜 (YYYY-MM-DD)
      participated?: boolean;        // 개인 참여 여부 (userId 지정 시만)
      participationRate: number;     // 전체 참여율 (0-100)
      submissions: number;           // 해당 날짜 총 제출 수
      userCount: number;             // 총 참여자 수
    }
  ]
  ```

---

## 챌린지 로그 (Challenge Logs)

### GET /api/challengeLogs
- **Description**: 챌린지 로그 목록 조회
- **Headers**
  - Authorization: Bearer {token}
- **Query Parameters**
  - page?: number (페이지 번호, 기본값: 0)
  - size?: number (페이지 크기, 기본값: 10)
  - challengeId?: string (챌린지 ID 필터)
  - userId?: string (사용자 ID 필터)
  - status?: string (상태 필터: 'PENDING', 'APPROVED', 'REJECTED')
- **Response Body**
  ```typescript
  {
    content: ChallengeLog[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
    numberOfElements: number;
    empty: boolean;
  }

  // ChallengeLog 타입
  interface ChallengeLog {
    id: string;                    // 로그 ID
    userId: string;                // 사용자 ID
    challengeId: string;           // 챌린지 ID
    content: string;               // 로그 내용
    imageUrl?: string;             // 이미지 URL
    status: 'pending' | 'approved' | 'rejected';  // 상태
    rejectionComment?: string;     // 반려 코멘트
    user: {                        // 사용자 정보
      id: string;
      name: string;
      avatar?: string;
    };
    createdAt: string;             // 생성일 (ISO 8601)
  }
  ```

### GET /api/challengeLogs/{id}
- **Description**: 특정 챌린지 로그 조회
- **Headers**
  - Authorization: Bearer {token}
- **Request Params**
  - id: number (챌린지 로그 ID)
- **Response Body**
  ```typescript
  ChallengeLog // 위의 ChallengeLog 인터페이스와 동일
  ```

### POST /api/challengeLogs
- **Description**: 챌린지 로그 생성 (사용자 제출)
- **Headers**
  - Authorization: Bearer {token}
  - Content-Type: application/json
- **Request Body**
  ```typescript
  {
    challengeId: string;   // 챌린지 ID
    content: string;       // 로그 내용 (10-2000자)
    imageUrl?: string;     // 이미지 URL (선택)
  }
  ```
- **Response Body**
  ```typescript
  ChallengeLog // 위의 ChallengeLog 인터페이스와 동일
  ```

### PUT /api/challengeLogs/{id}/approve
- **Description**: 챌린지 로그 승인
- **Headers**
  - Authorization: Bearer {token}
  - Content-Type: application/json
- **Request Params**
  - id: number (챌린지 로그 ID)
- **Request Body**
  ```typescript
  {
    comment?: string;      // 승인 코멘트 (선택, 최대 500자)
  }
  ```
- **Response Body**
  ```typescript
  ChallengeLog // 위의 ChallengeLog 인터페이스와 동일
  ```

### PUT /api/challengeLogs/{id}/reject
- **Description**: 챌린지 로그 반려
- **Headers**
  - Authorization: Bearer {token}
  - Content-Type: application/json
- **Request Params**
  - id: number (챌린지 로그 ID)
- **Request Body**
  ```typescript
  {
    comment: string;       // 반려 사유 (필수, 최대 500자)
  }
  ```
- **Response Body**
  ```typescript
  ChallengeLog // 위의 ChallengeLog 인터페이스와 동일
  ```

---

## 알림 (Notifications)

### GET /api/notifications
- **Description**: 사용자 알림 목록 조회
- **Headers**
  - Authorization: Bearer {token}
- **Query Parameters**
  - page?: number (페이지 번호, 기본값: 0)
  - size?: number (페이지 크기, 기본값: 10)
  - read?: boolean (읽음 상태 필터)
  - type?: string (알림 타입 필터)
- **Response Body**
  ```typescript
  {
    content: Notification[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
    numberOfElements: number;
    empty: boolean;
  }

  // Notification 타입
  interface Notification {
    id: string;                    // 알림 ID
    type: 'challenge_approved' | 'challenge_rejected' | 'challenge_joined' | 'challenge_started' | 'challenge_ended' | 'application_approved' | 'application_rejected' | 'system' | 'new_challenge_log' | 'new_application' | 'daily_reminder' | 'daily_approval_summary';
    title: string;                 // 알림 제목
    message: string;               // 알림 메시지
    read: boolean;                 // 읽음 여부
    createdAt: string;             // 생성일 (ISO 8601)
    userId: string;                // 수신자 ID
    relatedId?: string;            // 관련 ID
    actionUrl?: string;            // 액션 URL
  }
  ```

### GET /api/notifications/unread-count
- **Description**: 읽지 않은 알림 개수 조회
- **Headers**
  - Authorization: Bearer {token}
- **Response Body**
  ```typescript
  {
    unreadCount: number;   // 읽지 않은 알림 개수
  }
  ```

### PUT /api/notifications/{id}/read
- **Description**: 알림 읽음 처리
- **Headers**
  - Authorization: Bearer {token}
- **Request Params**
  - id: number (알림 ID)
- **Response Body**
  ```typescript
  Notification // 위의 Notification 인터페이스와 동일 (read: true)
  ```

### PUT /api/notifications/mark-all-read
- **Description**: 모든 알림 읽음 처리
- **Headers**
  - Authorization: Bearer {token}
- **Response Body**
  ```typescript
  {
    updatedCount: number;  // 읽음 처리된 알림 개수
  }
  ```

### POST /api/notifications
- **Description**: 새 알림 생성
- **Headers**
  - Authorization: Bearer {token}
  - Content-Type: application/json
- **Request Body**
  ```typescript
  {
    userId: string;        // 수신자 사용자 ID
    type: 'challenge_approved' | 'challenge_rejected' | 'group_joined' | 'group_started' | 'group_ended' | 'application_approved' | 'application_rejected' | 'system';
    title: string;         // 알림 제목 (1-100자)
    message: string;       // 알림 메시지 (1-500자)
    relatedId?: string;    // 관련 ID (challengeId, logId 등)
    actionUrl?: string;    // 액션 URL (최대 500자)
  }
  ```
- **Response Body**
  ```typescript
  Notification // 위의 Notification 인터페이스와 동일
  ```

### DELETE /api/notifications/{id}
- **Description**: 알림 삭제
- **Headers**
  - Authorization: Bearer {token}
- **Request Params**
  - id: number (알림 ID)
- **Response Body**
  ```typescript
  // 204 No Content (응답 본문 없음)
  ```

---

## 변경 로그

### 2025-09-22 20:30 KST: 챌린지 삭제 API 엔드포인트 추가 ✅
- ✅ **DELETE /api/challenges/{id}**: 챌린지 삭제 (리더 전용)
  - 리더 권한 검증 및 데이터 무결성 보장 로직
  - 연관 데이터 정리 (참여자, 인증 로그, 신청서, 알림)
  - 삭제 결과 응답 DTO (success, message, deletedAt) 구현
- ✅ **ChallengeService 확장**: deleteChallenge 메소드 추가
  - 리더 권한 검증 (403 Forbidden 에러 처리)
  - 존재하지 않는 챌린지 처리 (404 Not Found)
  - Hard delete 정책 적용으로 완전한 데이터 정리
- ✅ **Repository 확장**: 연관 데이터 삭제를 위한 @Modifying 쿼리 추가
  - ParticipationRepository.deleteByChallenge()
  - ChallengeApplicationRepository.deleteByChallenge()
  - ChallengeLogRepository.deleteByChallenge()
  - NotificationRepository.deleteByRelatedId()
- ✅ **DeleteChallengeResponse DTO**: 삭제 응답 전용 DTO 클래스 생성

### 2025-09-19 19:30 KST: 챌린지 통계 API 엔드포인트 추가 ✅
- ✅ **GET /api/challenges/{id}/members/stats**: 챌린지 멤버별 통계 조회 (Streak, 달성률)
  - 멤버별 연속 참여일, 달성률, 총 제출/승인 횟수, 마지막 제출일 제공
  - 권한 확인: 챌린지 조회 권한이 있는 사용자만 접근 가능
- ✅ **GET /api/challenges/{id}/participation**: 챌린지 참여율 데이터 조회
  - 일별 참여율, 제출 수, 전체 참여자 수 제공
  - userId 필터: 특정 사용자의 개인 참여 여부 확인 가능
  - 날짜 범위 필터: startDate, endDate로 조회 기간 제한 가능
- ✅ **MemberStatsDto.kt**: 통계 응답 DTO 클래스 추가
- ✅ **ChallengeService 확장**: getMemberStats, getParticipationStats 메소드 구현
- ✅ **ChallengeController 확장**: 2개 신규 엔드포인트 라우팅 추가

### 2025-09-19 18:00 KST: 회원가입 API 이메일 필드 추가 ✅
- ✅ **회원가입 Request Body 확장**: `email` 필드 추가 (유효한 이메일 형식 검증)
- ✅ **로그인 ID 검증 규칙 수정**: 2-20자, 영문/숫자/언더스코어/하이픈만 허용
- ✅ **프론트엔드 호환성 확보**: 프론트엔드 요구사항에 맞춘 API 구조 적용

### 2025-09-19 17:30 KST: Backend API_PROTOCOL.md 초기 생성 ✅
- ✅ **백엔드 실제 구현 기준 API 문서화**: 5개 컨트롤러, 27개 엔드포인트 완전 문서화
- ✅ **프론트엔드와 동일한 구조 적용**: Headers, Request, Response Body 통합 형식
- ✅ **실제 DTO 기반 정확한 스펙**: Kotlin DTO 클래스를 기반으로 한 정확한 타입 정의
- ✅ **백엔드 전용 기능 포함**:
  - 추가 사용자 관리 엔드포인트 (GET /api/users, /api/users/me/challenges 등)
  - 다양한 챌린지 참여 방식 (초대코드, 사용자ID 등)
  - 확장된 알림 타입 (new_challenge_log, daily_reminder 등)
  - 읽지 않은 알림 개수 조회
- ✅ **페이지네이션 표준화**: Spring Data의 Page<T> 구조 정확히 반영
- ✅ **검증 규칙 문서화**: 실제 @Valid 애노테이션 기반 입력 검증 규칙 포함

**중요**: 이 문서는 백엔드 실제 구현을 100% 반영하며, 프론트엔드 API_PROTOCOL.md와의 차이점을 명확히 보여줍니다.
# Backend API Specification

**Version**: 1.3.2
**Last Updated**: 2025-09-18
**Status**: Active
**Base URL**: `http://localhost:8888/api`

## Overview
This document defines the REST API specification for the Challenge MVP backend service. The API is built with Spring Boot and provides endpoints for user management, challenge groups, and activity logging with approval workflows.

## Change Log
- **v1.3.2** (2025-09-18): Modified invite code API to allow public access (no authentication required) for easier private challenge participation
- **v1.3.1** (2025-09-18): Added invite code-based challenge lookup API for private challenge participation flow
- **v1.3.0** (2025-09-17): Added Group Application approval system with 4 new endpoints and extended notification types
- **v1.2.1** (2025-09-17): Authentication API changes (email → loginId) and dedicated group join endpoint
- **v1.2.0** (2025-09-16): Added missing frontend-required APIs (logout, current user, group update) and corrected base URL
- **v1.1.0** (2025-09-16): Added Notification API endpoints for real-time notification system
- **v1.0.0** (2025-09-15): Initial API design aligned with frontend requirements and mock API compatibility

---

## Authentication

### JWT-based Authentication
- **Authorization Header**: `Authorization: Bearer <jwt-token>`
- **Token Expiration**: 24 hours
- **Refresh**: Re-login required (v1.0 limitation)

### Public Endpoints (No Auth Required)
- `POST /api/auth/signup`
- `POST /api/auth/signin`
- `GET /api/groups` (read-only access)
- `GET /api/groups/{id}` (read-only access)

### Protected Endpoints
- All other endpoints require valid JWT token
- Role-based access control applied where specified

---

## Data Models (API Response Format)

### User (Response DTO)
```typescript
interface User {
  id: string;           // Long converted to string
  name: string;         // User.nickname field
  loginId: string;      // Replaces email for authentication
  avatar?: string;      // User.avatarUrl field
  role: 'leader' | 'member';  // UserRole enum converted
  createdAt: string;    // ISO date format
}
```

### Group (Response DTO)
```typescript
interface Group {
  id: string;
  name: string;
  description: string;
  category: 'health' | 'study' | 'habit' | 'hobby' | 'social' | 'business';
  difficulty: 'easy' | 'medium' | 'hard';
  duration: number;     // days
  startDate: string;    // ISO date format
  endDate: string;      // ISO date format
  maxMembers: number;
  currentMembers: number;  // Calculated from participations
  leaderId: string;     // Leader user ID
  leader: User;         // Embedded leader info
  members: User[];      // Array of participating users
  status: 'recruiting' | 'active' | 'completed';
  coverImage?: string;  // CoverImageUrl field
  reward?: string;
  tags?: string[];
  createdAt: string;    // ISO date format
}
```

### ChallengeLog (Response DTO)
```typescript
interface ChallengeLog {
  id: string;
  userId: string;
  groupId: string;
  content: string;
  imageUrl?: string;
  status: 'pending' | 'approved' | 'rejected';
  rejectionComment?: string;
  user: {              // Embedded user info
    id: string;
    name: string;
    avatar?: string;
  };
  createdAt: string;   // ISO date format
}
```

### GroupApplication (Response DTO)
```typescript
interface GroupApplication {
  id: string;
  groupId: string;
  userId: string;
  reason: string;
  status: 'pending' | 'approved' | 'rejected';
  createdAt: string;   // ISO date format
  reviewedAt?: string; // ISO date format
  rejectionReason?: string;
  user: {              // Embedded user info
    id: string;
    name: string;
    avatar?: string;
  };
}
```

### Notification (Response DTO)
```typescript
interface Notification {
  id: string;
  type: 'challenge_approved' | 'challenge_rejected' | 'group_joined' | 'group_started' | 'group_ended' | 'application_approved' | 'application_rejected' | 'system';
  title: string;
  message: string;
  read: boolean;
  createdAt: string;   // ISO date format
  userId: string;
  relatedId?: string;  // Related entity ID (group, challengeLog, applicationId, etc.)
  actionUrl?: string;  // URL for notification action
}
```

---

## API Endpoints

### Authentication Endpoints

#### POST /api/auth/signup
Register a new user account.

**Request Body:**
```typescript
{
  loginId: string;      // 3-30 characters, unique login identifier
  password: string;     // 8-50 characters
  nickname: string;     // 2-20 characters
  role?: 'LEADER' | 'MEMBER';  // Default: 'MEMBER'
}
```

**Response (201 Created):**
```typescript
{
  token: string;        // JWT access token
  user: User;          // User profile data
}
```

**Errors:**
- `400 Bad Request`: Invalid input data, loginId already exists
- `500 Internal Server Error`: Registration failed

#### POST /api/auth/signin
Authenticate existing user.

**Request Body:**
```typescript
{
  id: string;          // Login ID (replaces email)
  password: string;
}
```

**Response (200 OK):**
```typescript
{
  token: string;        // JWT access token
  user: User;          // User profile data
}
```

**Errors:**
- `401 Unauthorized`: Invalid credentials
- `400 Bad Request`: Missing id/password

#### POST /api/auth/logout
Log out current user (JWT stateless - client-side token removal).

**Request Body:** None

**Response (200 OK):**
```typescript
{
  message: string;     // Success message
}
```

**Errors:**
- `401 Unauthorized`: Authentication required

---

### User Endpoints

#### GET /api/users
Get all users (paginated).

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 10)
- `role`: Filter by role ('LEADER' | 'MEMBER')

**Response (200 OK):**
```typescript
{
  content: User[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
}
```

#### GET /api/users/{id}
Get user by ID.

**Path Parameters:**
- `id`: User ID (Long)

**Response (200 OK):** `User`

**Errors:**
- `404 Not Found`: User not found

#### GET /api/users/me
Get current authenticated user profile.
**Auth Required**: Yes

**Response (200 OK):** `User`

**Errors:**
- `401 Unauthorized`: Authentication required

#### GET /api/users/me/applications
Get current user's group applications.
**Auth Required**: Yes

**Query Parameters:**
- `status`: Filter by status ('pending' | 'approved' | 'rejected')
- `page`: Page number (default: 0)
- `size`: Page size (default: 10)

**Response (200 OK):**
```typescript
{
  content: GroupApplication[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
}
```

**Errors:**
- `401 Unauthorized`: Authentication required

---

### Group Endpoints

#### GET /api/groups
Get all challenge groups.

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 10)
- `category`: Filter by category
- `status`: Filter by status
- `search`: Search in name/description

**Response (200 OK):**
```typescript
{
  content: Group[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
}
```

#### GET /api/groups/{id}
Get challenge group by ID.

**Path Parameters:**
- `id`: Group ID (Long)

**Response (200 OK):** `Group`

**Errors:**
- `404 Not Found`: Group not found

#### POST /api/groups
Create new challenge group.
**Auth Required**: Yes (any authenticated user)

**Request Body:**
```typescript
{
  name: string;                    // 2-100 characters
  description: string;             // 10-1000 characters
  category: 'health' | 'study' | 'habit' | 'hobby' | 'social' | 'business';
  difficulty: 'easy' | 'medium' | 'hard';
  duration: number;                // 1-365 days
  startDate: string;               // ISO date, future date
  endDate: string;                 // ISO date, after startDate
  maxMembers: number;              // 2-1000
  coverImage?: string;             // Valid URL
  reward?: string;                 // Optional reward description
  tags?: string[];                 // Optional tags array
}
```

**Response (201 Created):** `Group`

**Errors:**
- `400 Bad Request`: Validation errors
- `401 Unauthorized`: Authentication required

#### POST /api/groups/{id}/join
⚠️ **DEPRECATED**: Use POST /api/groups/{id}/apply for approval-based joining
Join a challenge group by user ID (direct join without approval).
**Auth Required**: Yes (for authentication, but user ID can be different)

**Path Parameters:**
- `id`: Group ID (Long)

**Request Body:**
```typescript
{
  userId: string;       // User ID to join the group
  joinReason?: string;  // Optional reason for joining
}
```

**Response (200 OK):** `Group` (updated with new member)

**Errors:**
- `400 Bad Request`: Already joined, group full, invalid status
- `404 Not Found`: Group not found, user not found
- `401 Unauthorized`: Authentication required

#### POST /api/groups/{id}/apply
Submit an application to join a challenge group (requires approval).
**Auth Required**: Yes

**Path Parameters:**
- `id`: Group ID (Long)

**Request Body:**
```typescript
{
  reason: string;       // Required reason for application (10-500 characters)
}
```

**Response (201 Created):** `GroupApplication`

**Errors:**
- `400 Bad Request`: Already applied, already member, group full, invalid status
- `404 Not Found`: Group not found
- `401 Unauthorized`: Authentication required

#### GET /api/groups/{id}/applications
Get all applications for a challenge group (Leader only).
**Auth Required**: Yes (Group Leader only)

**Path Parameters:**
- `id`: Group ID (Long)

**Query Parameters:**
- `status`: Filter by status ('pending' | 'approved' | 'rejected')
- `page`: Page number (default: 0)
- `size`: Page size (default: 10)

**Response (200 OK):**
```typescript
{
  content: GroupApplication[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
}
```

**Errors:**
- `403 Forbidden`: Not group leader
- `404 Not Found`: Group not found
- `401 Unauthorized`: Authentication required

#### PUT /api/groups/{id}/applications/{applicationId}/status
Approve or reject a group application (Leader only).
**Auth Required**: Yes (Group Leader only)

**Path Parameters:**
- `id`: Group ID (Long)
- `applicationId`: Application ID (Long)

**Request Body:**
```typescript
{
  status: 'approved' | 'rejected';
  rejectionReason?: string;  // Required if status is 'rejected'
}
```

**Response (200 OK):** `GroupApplication` (updated status)
**Side Effects:** If approved, user is automatically added to group as member

**Errors:**
- `400 Bad Request`: Invalid status, missing rejection reason, already processed
- `403 Forbidden`: Not group leader
- `404 Not Found`: Group not found, application not found
- `401 Unauthorized`: Authentication required

#### PUT /api/groups/{id}/join
Join a challenge group (legacy endpoint for backwards compatibility).
**Auth Required**: Yes

**Path Parameters:**
- `id`: Group ID (Long)

**Request Body:**
```typescript
{
  joinReason?: string;  // Optional reason for joining
}
```

**Response (200 OK):** `Group` (updated with new member)

**Errors:**
- `400 Bad Request`: Already joined, group full, invalid status
- `404 Not Found`: Group not found
- `401 Unauthorized`: Authentication required

#### PUT /api/groups/{id}/leave
Leave a challenge group.
**Auth Required**: Yes

**Path Parameters:**
- `id`: Group ID (Long)

**Response (200 OK):** `Group` (updated without member)

**Errors:**
- `400 Bad Request`: Not a member, cannot leave as leader
- `404 Not Found`: Group not found

#### PUT /api/groups/{id}
Update a challenge group.
**Auth Required**: Yes (Group Leader only)

**Path Parameters:**
- `id`: Group ID (Long)

**Request Body:**
```typescript
{
  name?: string;                    // Optional, 2-100 characters
  description?: string;             // Optional, 10-1000 characters
  category?: 'health' | 'study' | 'habit' | 'hobby' | 'social' | 'business';
  difficulty?: 'easy' | 'medium' | 'hard';
  duration?: number;                // Optional, 1-365 days
  startDate?: string;               // Optional, ISO date, future date
  endDate?: string;                 // Optional, ISO date, after startDate
  maxMembers?: number;              // Optional, 2-1000
  coverImage?: string;              // Optional, valid URL
  reward?: string;                  // Optional reward description
  tags?: string[];                  // Optional tags array
}
```

**Response (200 OK):** `Group` (updated)

**Errors:**
- `400 Bad Request`: Validation errors
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Not group leader
- `404 Not Found`: Group not found

#### GET /api/challenges/invite/{inviteCode}
Get challenge information by invite code for private challenge participation.
**Auth Required**: No (Public access)

**Path Parameters:**
- `inviteCode`: Challenge invite code (string, 8 characters, uppercase letters and numbers)

**Response (200 OK):** `Group` (challenge information)

**Errors:**
- `404 Not Found`: Invalid or expired invite code

**Usage:**
This endpoint allows anyone with an invite code to lookup private challenge information before deciding to participate. No authentication is required - having the invite code provides sufficient access. Used in the frontend participation flow where users enter an invite code to preview the challenge details.

---

### Challenge Log Endpoints

#### GET /api/challengeLogs
Get challenge logs with filtering.

**Query Parameters:**
- `groupId`: Filter by group ID
- `userId`: Filter by user ID
- `status`: Filter by status ('pending' | 'approved' | 'rejected')
- `page`: Page number (default: 0)
- `size`: Page size (default: 10)

**Response (200 OK):**
```typescript
{
  content: ChallengeLog[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
}
```

#### GET /api/challengeLogs/{id}
Get challenge log by ID.

**Path Parameters:**
- `id`: Challenge log ID (Long)

**Response (200 OK):** `ChallengeLog`

**Errors:**
- `404 Not Found`: Challenge log not found

#### POST /api/challengeLogs
Create new challenge log (user submission).
**Auth Required**: Yes

**Request Body:**
```typescript
{
  groupId: string;      // Group ID (must be member)
  content: string;      // 10-2000 characters
  imageUrl?: string;    // Optional image URL
}
```

**Response (201 Created):** `ChallengeLog`

**Errors:**
- `400 Bad Request`: Not a group member, invalid data
- `404 Not Found`: Group not found
- `401 Unauthorized`: Authentication required

#### PUT /api/challengeLogs/{id}/approve
Approve a challenge log.
**Auth Required**: Yes (Group Leader only)

**Path Parameters:**
- `id`: Challenge log ID (Long)

**Request Body:**
```typescript
{
  comment?: string;  // Optional approval comment
}
```

**Response (200 OK):** `ChallengeLog` (status updated to 'approved')

**Errors:**
- `403 Forbidden`: Not group leader
- `400 Bad Request`: Already processed
- `404 Not Found`: Challenge log not found

#### PUT /api/challengeLogs/{id}/reject
Reject a challenge log.
**Auth Required**: Yes (Group Leader only)

**Path Parameters:**
- `id`: Challenge log ID (Long)

**Request Body:**
```typescript
{
  comment: string;   // Required rejection reason
}
```

**Response (200 OK):** `ChallengeLog` (status updated to 'rejected')

**Errors:**
- `403 Forbidden`: Not group leader
- `400 Bad Request`: Already processed, missing comment
- `404 Not Found`: Challenge log not found

---

### Notification Endpoints

#### GET /api/notifications
Get user's notifications with filtering.
**Auth Required**: Yes

**Query Parameters:**
- `read`: Filter by read status (true | false)
- `type`: Filter by notification type
- `page`: Page number (default: 0)
- `size`: Page size (default: 10)

**Response (200 OK):**
```typescript
{
  content: Notification[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
}
```

**Errors:**
- `401 Unauthorized`: Authentication required

#### PUT /api/notifications/{id}/read
Mark a notification as read.
**Auth Required**: Yes

**Path Parameters:**
- `id`: Notification ID (Long)

**Response (200 OK):** `Notification` (updated with read: true)

**Errors:**
- `404 Not Found`: Notification not found
- `403 Forbidden`: Not notification owner
- `401 Unauthorized`: Authentication required

#### PUT /api/notifications/mark-all-read
Mark all user's notifications as read.
**Auth Required**: Yes

**Response (200 OK):**
```typescript
{
  updatedCount: number;  // Number of notifications marked as read
}
```

**Errors:**
- `401 Unauthorized`: Authentication required

#### POST /api/notifications
Create a new notification.
**Auth Required**: Yes (System/Admin use only)

**Request Body:**
```typescript
{
  userId: string;       // Target user ID
  type: 'challenge_approved' | 'challenge_rejected' | 'group_joined' | 'group_started' | 'group_ended' | 'application_approved' | 'application_rejected' | 'system';
  title: string;        // 1-100 characters
  message: string;      // 1-500 characters
  relatedId?: string;   // Optional related entity ID
  actionUrl?: string;   // Optional action URL
}
```

**Response (201 Created):** `Notification`

**Errors:**
- `400 Bad Request`: Invalid data, user not found
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions

#### DELETE /api/notifications/{id}
Delete a notification.
**Auth Required**: Yes

**Path Parameters:**
- `id`: Notification ID (Long)

**Response (204 No Content)**

**Errors:**
- `404 Not Found`: Notification not found
- `403 Forbidden`: Not notification owner
- `401 Unauthorized`: Authentication required

---

## Error Response Format

### Standard Error Response
```typescript
interface ErrorResponse {
  timestamp: string;    // ISO timestamp
  status: number;      // HTTP status code
  error: string;       // Error type
  message: string;     // User-friendly message
  path: string;        // Request path
}
```

### Validation Error Response
```typescript
interface ValidationErrorResponse extends ErrorResponse {
  validationErrors: {
    field: string;     // Field name
    message: string;   // Validation message
    rejectedValue: any; // Invalid value
  }[];
}
```

---

## Status Codes

### Success Codes
- `200 OK`: Successful GET, PUT requests
- `201 Created`: Successful POST requests
- `204 No Content`: Successful DELETE requests

### Client Error Codes
- `400 Bad Request`: Invalid request data, validation errors
- `401 Unauthorized`: Authentication required or failed
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource conflict (duplicate email, etc.)

### Server Error Codes
- `500 Internal Server Error`: Unexpected server error

---

## Rate Limiting
**Future Enhancement** (Not implemented in v1.0)
- General API: 100 requests/minute per user
- Upload endpoints: 10 requests/minute per user
- Authentication endpoints: 5 requests/minute per IP

---

## Data Validation

### Request Validation
- All request bodies validated using Spring Boot Validation
- Field-level validation with custom error messages
- Type safety enforced through DTOs

### Business Logic Validation
- User authorization checks (group membership, leadership)
- Status transitions (pending → approved/rejected)
- Date constraints (start < end, future dates)
- Capacity limits (max members)

---

## Pagination Format

### Request Parameters
- `page`: Zero-based page number (default: 0)
- `size`: Items per page (default: 10, max: 100)
- `sort`: Sort criteria (field,direction) e.g., "createdAt,desc"

### Response Format
```typescript
interface PageResponse<T> {
  content: T[];           // Array of items
  totalElements: number;   // Total count
  totalPages: number;      // Total pages
  currentPage: number;     // Current page (0-based)
  size: number;           // Page size
  first: boolean;         // Is first page
  last: boolean;          // Is last page
}
```

---

## Migration from Mock API

### Compatibility Notes
1. **String IDs**: Backend Long IDs converted to string in responses
2. **Nested Objects**: Full object embedding for leader/user relationships
3. **Calculated Fields**: `currentMembers` calculated from participation count
4. **Date Formats**: All dates in ISO 8601 format
5. **Status Enums**: Case-insensitive enum mapping (PENDING → pending)

### Breaking Changes from Mock API
- **Authentication Required**: Most endpoints now require JWT token
- **Structured Errors**: Detailed error responses with validation info
- **Pagination**: All list endpoints now paginated
- **Role Restrictions**: Group management restricted to leaders

---

## Version History
- **v1.3.2** (2025-09-18): Modified invite code API to allow public access without authentication for easier private challenge participation flow
- **v1.3.1** (2025-09-18): Added invite code-based challenge lookup API (GET /api/challenges/invite/{inviteCode}) for private challenge participation flow alignment with frontend
- **v1.3.0** (2025-09-17): Added Group Application approval system (4 new endpoints), extended notification types (application_approved, application_rejected), deprecated direct join
- **v1.2.1** (2025-09-17): Authentication API changes (email → loginId) and dedicated group join endpoint with user ID parameter
- **v1.2.0** (2025-09-16): Added missing frontend-required APIs (auth logout, users/me, groups update) and corrected base URL to 8888
- **v1.1.0** (2025-09-16): Added Notification API endpoints with full CRUD support and read status management
- **v1.0.0** (2025-09-15): Initial API design with full frontend compatibility and JWT authentication
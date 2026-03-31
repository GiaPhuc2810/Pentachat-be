# ⚙️ CONVENTION – CODE GENERATION (FINAL)

## 1. Mục tiêu
- File này dùng **để gen code & code thống nhất**
- Không dùng làm tài liệu quản lý hay báo cáo
- Ưu tiên **đơn giản – rõ ràng – một stack duy nhất**

---

## 2. Stack (CHỐT)
- Backend: **Java (Spring Boot)**
- Realtime: **WebSocket (STOMP)**
- Frontend: **JavaScript (Vanilla JS / HTML)**
- Database: **SQL Server**
- Build Tool: **Maven**

---

## 3. Quy ước đặt tên (Naming)

### Java Backend
- Package: `lowercase` (vd: `com.hdtpt.pentachat`)
- Class / Interface: `PascalCase` (vd: `AuthController`, `UserService`)
- Method / Variable: `camelCase` (vd: `getUserById`, `userName`)
- Constant: `UPPER_SNAKE_CASE` (vd: `MAX_RETRY_COUNT`)
- File: `PascalCase.java` (theo tên class)

### Frontend
- File HTML/CSS/JS: `kebab-case` (vd: `auth.js`, `chat.js`)
- Function / Variable: `camelCase`
- Constant: `UPPER_SNAKE_CASE`

### Database
- Table name: `snake_case` số nhiều (vd: `users`, `messages`)
- Column name: `camelCase` trong Java Entity, `snake_case` trong DB
- Primary key: `id`
- Foreign key: `{entity}Id` trong Java (vd: `userId`, `fromUserId`)

---

## 4. API Convention
- RESTful API
- Base URL: `/api` (khuyến nghị, hiện tại chưa dùng)
- Resource-based:
  - `/api/auth` (authentication)
  - `/api/messages` (messaging)
  - `/api/wallets` (wallet operations)
  - `/api/transactions` (transaction history)
- HTTP Method:
  - GET: lấy dữ liệu
  - POST: tạo mới
  - PUT: cập nhật toàn bộ
  - PATCH: cập nhật một phần
  - DELETE: xóa

### Response chuẩn
```json
{
  "success": true,
  "message": "OK",
  "data": {}
}
```

**Implementation**: Sử dụng `ApiResponse` DTO
```java
@Data
@Builder
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;
}
```

---

## 5. Authentication

### 5.1. Hiện tại
- Session-based authentication
- Session được quản lý bởi SessionManager (in-memory)
- SessionId trả về sau login
- Client lưu sessionId (localStorage hoặc cookie)
- Không dùng JWT ở giai đoạn này

<!-- ### 5.2. Tương lai
- Nâng cấp sang JWT authentication
- Header: Authorization: Bearer <token>
- Stateless, hỗ trợ scale nhiều instance -->

## 6. Realtime (WebSocket STOMP)
- Protocol: **STOMP over WebSocket**
- Endpoint: `/ws` (với SockJS fallback)
- Message destination prefix: `/app`
- Subscription prefix: `/topic`
- Event naming: `kebab-case` hoặc `dot.notation`
  - Ví dụ: `/app/chat.send`, `/topic/messages`
- WebSocket chỉ dùng cho:
  - Chat realtime
  - Notification realtime
- Không xử lý logic nghiệp vụ phức tạp trong WebSocket handler

---

## 7. Database Convention
- Table name: `snake_case` (số nhiều)
- Primary key: `id` (long)
- **KHÔNG dùng Foreign Key constraint** trong database
  - Lưu reference bằng ID field (vd: `userId`, `fromUserId`, `toUserId`)
  - Xử lý quan hệ và validation trên **code/application layer**
  - Không dùng `@ManyToOne`, `@OneToMany` trong JPA Entity
- Mỗi bảng **NÊN CÓ**:
  - `created_at` (LocalDateTime)
  - `updated_at` (LocalDateTime)

### Ví dụ:
```java
@Entity
@Table(name = "messages")
public class Message {
    @Id
    private String id;
    
    // Không dùng @ManyToOne, chỉ lưu ID
    @Column(nullable = false)
    private String fromUserId;  // Reference đến User
    
    @Column(nullable = false)
    private String toUserId;    // Reference đến User
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

---

## 8. Cấu trúc thư mục (BẮT BUỘC)

### Backend (Spring Boot)
```
src/main/java/com/hdtpt/pentachat/
├─ auth/
│  ├─ controller/
│  │  └─ AuthController.java
│  ├─ service/
│  │  └─ AuthService.java
│  └─ dto/
│     ├─ request/
│     │  ├─ LoginRequest.java
│     │  └─ RegisterRequest.java
│     └─ response/
│        └─ AuthResponse.java
├─ message/
│  ├─ controller/
│  ├─ service/
│  ├─ model/
│  ├─ repository/
│  └─ dto/
├─ wallet/
│  ├─ controller/
│  ├─ service/
│  ├─ model/
│  ├─ repository/
│  └─ dto/
├─ websocket/
│  ├─ WebSocketConfig.java
│  └─ WebSocketController.java
├─ config/
│  └─ CorsConfig.java
├─ security/
│  └─ SessionManager.java
├─ dto/
│  └─ response/
│     └─ ApiResponse.java
└─ exception/
   ├─ AppException.java
   └─ GlobalExceptionHandler.java

src/main/resources/
├─ application.properties
└─ static/

pom.xml
```

### Frontend
```
frontend/
├─ src/
│  ├─ pages/
│  ├─ components/
│  ├─ api/
│  └─ main.jsx
└─ package.json
```

---

## 9. Nguyên tắc gen code
- 1 API endpoint = 1 function
- Controller:
  - nhận request
  - trả response
- Service:
  - xử lý logic
- Không hard-code dữ liệu
- Không over-engineering
- Sử dụng Lombok để giảm boilerplate code
- Luôn handle exceptions với `@ControllerAdvice`

---

## 10. Exception Handling
- Sử dụng `@ControllerAdvice` cho global exception handling
- Custom exception: `AppException`
- Trả về format thống nhất:

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse> handleAppException(AppException ex) {
        return ResponseEntity
            .status(ex.getStatus())
            .body(ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build());
    }
}
```

---

## 11. Dependency Injection
- Sử dụng **Constructor Injection** (khuyến nghị)
- Không dùng `@Autowired` trên field

```java
// ✅ ĐÚNG
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

// ❌ SAI
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
}
```

---

## 12. Ghi chú
- File này là **chuẩn duy nhất** cho toàn bộ project
- Khi gen code bằng AI phải tuân theo file này
- Ưu tiên code đơn giản, dễ đọc, dễ maintain
- Follow Spring Boot best practices
- Sử dụng Lombok để giảm boilerplate
- Luôn validate input
- Luôn handle exceptions properly
# 🔐 PHÂN TÍCH & KHUYẾN NGHỊ BẢO MẬT HỆ THỐNG

## 📊 Đánh Giá Hiện Trạng

### ✅ Những Gì Đã Làm Đúng

#### 1. **Phân Quyền Dựa Trên Role**
```java
public enum UserRole {
    MANAGER,   // Quản lý - quyền cao nhất
    STAFF,     // Nhân viên - xử lý nghiệp vụ
    CUSTOMER   // Khách hàng - sử dụng dịch vụ
}
```

**✅ Đánh giá**: Cấu trúc role đơn giản, rõ ràng, phù hợp cho hệ thống quản lý sân cầu lông.

#### 2. **Session-Based Authentication với Cookie**
```java
SecurityContext context = SecurityContextHolder.createEmptyContext();
context.setAuthentication(authentication);
SecurityContextHolder.setContext(context);
securityContextRepository.saveContext(context, httpRequest, httpResponse);
```

**✅ Đánh giá**: 
- Sử dụng `HttpSessionSecurityContextRepository` - chuẩn Spring Security
- Session được lưu tự động trong cookie `JSESSIONID`
- Khi đăng xuất, session được xóa tự động
- **Cách tiếp cận này ĐÚNG và phù hợp cho web application truyền thống**

#### 3. **Password Security**
```java
private final PasswordEncoder passwordEncoder; // BCrypt
```

**✅ Đánh giá**: BCrypt là lựa chọn tốt, an toàn cho password hashing.

---

## ⚠️ VẤN ĐỀ CẦN KHẮC PHỤC NGAY

### 🔴 CRITICAL - Ưu Tiên Cao

#### 1. **Role Lưu Dạng String Thay Vì Enum**

**Vấn đề hiện tại**:
```java
// Entity
@Column(name = "role", nullable = false, length = 20)
private String role;  // ❌ Lưu "CUSTOMER", "MANAGER", "STAFF"

// Service
.role(String.valueOf(UserRole.CUSTOMER))  // ❌ Convert enum sang String
```

**Tại sao đây là vấn đề?**
- ❌ Mất type safety - có thể gán giá trị bất kỳ
- ❌ Dễ gây lỗi typo: "CUSTMER", "customer", "Customer"
- ❌ Khó maintain khi thêm role mới
- ❌ Không tận dụng được ưu điểm của Enum
- ❌ Code review khó phát hiện lỗi

**✅ Giải pháp được khuyến nghị**:

```java
// Entity - CÁCH 1: Sử dụng @Enumerated
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // ✅ THAY ĐỔI NÀY
    @Enumerated(EnumType.STRING)  // Lưu dạng "CUSTOMER", "MANAGER", "STAFF"
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;  // ✅ Dùng trực tiếp Enum
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;  // ✅ Tương tự cho Status
}

// Service - Cách sử dụng mới
User user = User.builder()
    .fullName(request.getFullName().trim())
    .phoneNumber(request.getPhoneNumber().trim())
    .email(request.getEmail().trim().toLowerCase())
    .passwordHash(passwordEncoder.encode(request.getPassword()))
    .role(UserRole.CUSTOMER)  // ✅ Trực tiếp dùng enum
    .status(UserStatus.ACTIVE)  // ✅ Trực tiếp dùng enum
    .build();
```

**Lợi ích**:
- ✅ Type safety - compiler kiểm tra lỗi
- ✅ Autocomplete trong IDE
- ✅ Không thể gán giá trị sai
- ✅ Dễ refactor
- ✅ Code clean hơn

**Migration cần thiết**:
```sql
-- Không cần migration vì data đã đúng format
-- Chỉ cần sửa code Java
```

---

#### 2. **Security Configuration Thiếu Roles Prefix**

**Vấn đề hiện tại**:
```java
.requestMatchers(MANAGER_URLS).hasRole("MANAGER")
.requestMatchers(STAFF_URLS).hasAnyRole("MANAGER","STAFF")
```

**Spring Security tự động thêm prefix "ROLE_"**, nên:
- `hasRole("MANAGER")` → tìm authority `ROLE_MANAGER`
- Nhưng trong DB bạn lưu: `MANAGER` (không có prefix)
- ❌ **Phân quyền sẽ không hoạt động!**

**✅ Giải pháp 1: Sử dụng hasAuthority() thay vì hasRole()**
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(PUBLIC_URLS).permitAll()
            // ✅ Dùng hasAuthority() - không tự động thêm prefix
            .requestMatchers(MANAGER_URLS).hasAuthority("MANAGER")
            .requestMatchers(STAFF_URLS).hasAnyAuthority("MANAGER", "STAFF")
            .requestMatchers(CUSTOMER_URLS).hasAnyAuthority("MANAGER", "STAFF", "CUSTOMER")
            .anyRequest().authenticated()
        )
        .logout(logout -> logout
            .logoutUrl("/auth/logout")
            .logoutSuccessHandler((request, response, authentication) -> {
                response.setStatus(HttpServletResponse.SC_OK);
            })
        );
    
    return http.build();
}
```

**✅ Giải pháp 2: Thêm prefix vào CustomUserDetails**
```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    // ✅ Thêm prefix "ROLE_" để dùng được hasRole()
    return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
}
```

**Khuyến nghị**: Dùng **Giải pháp 1** vì đơn giản và rõ ràng hơn.

---

#### 3. **CustomUserDetailsService Cần Kiểm Tra Status**

**Vấn đề hiện tại**:
```java
@Override
public UserDetails loadUserByUsername(String username) {
    User user = // ... tìm user
    
    // ❌ THIẾU: Không kiểm tra user.getStatus()
    return new CustomUserDetails(user);
}
```

**✅ Giải pháp**:
```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(username.toLowerCase())
        .or(() -> userRepository.findByPhoneNumber(username))
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    
    // ✅ THÊM: Kiểm tra status
    return CustomUserDetails.builder()
        .user(user)
        .enabled(user.getStatus() == UserStatus.ACTIVE)  // ✅ Disable nếu BLOCKED
        .accountNonLocked(user.getStatus() != UserStatus.BLOCKED)
        .build();
}
```

---

### 🟡 MEDIUM - Cần Cải Thiện

#### 4. **OTP Security Yếu**

**Vấn đề hiện tại**:
```java
// ❌ Lưu OTP dạng plaintext
@Column(name = "reset_otp", length = 6)
private String resetOtp;

// ❌ Không có expiry time
user.setResetOtp(otp);
userRepository.save(user);
```

**Rủi ro**:
- OTP có thể bị đánh cắp từ database
- OTP không bao giờ hết hạn
- Có thể brute force (chỉ 1 triệu combinations)
- Không giới hạn số lần thử

**✅ Giải pháp được khuyến nghị**:

```java
// Entity - Thêm trường expiry
@Entity
@Table(name = "users")
public class User {
    
    @Column(name = "reset_otp", length = 60)  // ✅ Tăng length cho BCrypt hash
    private String resetOtpHash;  // ✅ Lưu hash thay vì plaintext
    
    @Column(name = "reset_otp_expires_at")
    private LocalDateTime resetOtpExpiresAt;  // ✅ Thời gian hết hạn
    
    @Column(name = "reset_otp_attempts", nullable = false)
    private int resetOtpAttempts = 0;  // ✅ Đếm số lần thử sai
}
```

```java
// Service - Cải thiện forgotPassword
public void forgotPassword(RequestForgotPassword request) {
    String email = request.getEmail().trim().toLowerCase();
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new InvalidDataException("Email không tồn tại"));
    
    // ✅ Kiểm tra rate limiting - tránh spam
    if (user.getResetOtpExpiresAt() != null && 
        user.getResetOtpExpiresAt().isAfter(LocalDateTime.now().minusMinutes(1))) {
        throw new InvalidDataException("Vui lòng đợi 1 phút trước khi yêu cầu lại");
    }
    
    // Tạo OTP 6 số
    String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
    
    // ✅ Hash OTP trước khi lưu
    user.setResetOtpHash(passwordEncoder.encode(otp));
    
    // ✅ OTP hết hạn sau 10 phút
    user.setResetOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
    
    // ✅ Reset số lần thử
    user.setResetOtpAttempts(0);
    
    userRepository.save(user);
    
    // Gửi OTP qua email (plaintext để user nhập)
    emailService.sendMailResetPasswordOtp(email, otp);
}
```

```java
// Service - Cải thiện resetPassword
public void resetPassword(RequestResetPassword request) {
    String email = request.getEmail().trim().toLowerCase();
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new InvalidDataException("Email không tồn tại"));
    
    // ✅ Kiểm tra OTP có tồn tại không
    if (user.getResetOtpHash() == null || user.getResetOtpExpiresAt() == null) {
        throw new InvalidDataException("Không có yêu cầu đổi mật khẩu");
    }
    
    // ✅ Kiểm tra OTP đã hết hạn chưa
    if (user.getResetOtpExpiresAt().isBefore(LocalDateTime.now())) {
        user.setResetOtpHash(null);
        user.setResetOtpExpiresAt(null);
        userRepository.save(user);
        throw new InvalidDataException("Mã OTP đã hết hạn");
    }
    
    // ✅ Giới hạn số lần thử (chống brute force)
    if (user.getResetOtpAttempts() >= 5) {
        user.setResetOtpHash(null);
        user.setResetOtpExpiresAt(null);
        user.setResetOtpAttempts(0);
        userRepository.save(user);
        throw new InvalidDataException("Đã nhập sai quá 5 lần. Vui lòng yêu cầu mã mới");
    }
    
    // ✅ Verify OTP bằng BCrypt
    String requestOtp = request.getOtp().trim();
    if (!passwordEncoder.matches(requestOtp, user.getResetOtpHash())) {
        user.setResetOtpAttempts(user.getResetOtpAttempts() + 1);
        userRepository.save(user);
        throw new InvalidDataException(
            String.format("Mã OTP không đúng. Còn %d lần thử", 5 - user.getResetOtpAttempts())
        );
    }
    
    // ✅ Kiểm tra password match
    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
        throw new InvalidDataException("Mật khẩu xác nhận không khớp");
    }
    
    // ✅ Cập nhật password và xóa OTP
    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    user.setResetOtpHash(null);
    user.setResetOtpExpiresAt(null);
    user.setResetOtpAttempts(0);
    
    userRepository.save(user);
}
```

**Migration cần thiết**:
```sql
-- V4__Improve_OTP_Security.sql
ALTER TABLE users
    RENAME COLUMN reset_otp TO reset_otp_hash;

ALTER TABLE users
    ALTER COLUMN reset_otp_hash TYPE VARCHAR(60);

ALTER TABLE users
    ADD COLUMN reset_otp_expires_at TIMESTAMP;

ALTER TABLE users
    ADD COLUMN reset_otp_attempts INT DEFAULT 0 NOT NULL;
```

---

#### 5. **Session Configuration Cần Cải Thiện**

**Vấn đề hiện tại**:
- Không có timeout configuration
- Không có concurrent session control
- Không có remember-me

**✅ Giải pháp**:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(PUBLIC_URLS).permitAll()
            .requestMatchers(MANAGER_URLS).hasAuthority("MANAGER")
            .requestMatchers(STAFF_URLS).hasAnyAuthority("MANAGER", "STAFF")
            .requestMatchers(CUSTOMER_URLS).hasAnyAuthority("MANAGER", "STAFF", "CUSTOMER")
            .anyRequest().authenticated()
        )
        
        // ✅ Session management
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .invalidSessionUrl("/auth/login")
            .maximumSessions(1)  // Chỉ cho phép 1 session/user
            .maxSessionsPreventsLogin(false)  // Session mới đẩy session cũ ra
            .expiredUrl("/auth/login")
        )
        
        // ✅ Remember Me (optional)
        .rememberMe(remember -> remember
            .key("uniqueAndSecretKey")  // Nên lưu trong env variable
            .tokenValiditySeconds(7 * 24 * 60 * 60)  // 7 ngày
            .rememberMeParameter("remember-me")
        )
        
        .logout(logout -> logout
            .logoutUrl("/auth/logout")
            .deleteCookies("JSESSIONID", "remember-me")  // ✅ Xóa cả remember-me cookie
            .invalidateHttpSession(true)  // ✅ Invalidate session
            .clearAuthentication(true)  // ✅ Clear authentication
            .logoutSuccessHandler((request, response, authentication) -> {
                response.setStatus(HttpServletResponse.SC_OK);
            })
        );
    
    return http.build();
}
```

**application.properties** - Thêm cấu hình:
```properties
# Session timeout (30 phút không hoạt động)
server.servlet.session.timeout=30m

# Cookie settings
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true  # Chỉ khi dùng HTTPS
server.servlet.session.cookie.same-site=strict
```

---

#### 6. **Login Response Nên Trả Về Session Info**

**Vấn đề hiện tại**:
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "id": 1,
    "fullName": "Nguyễn Văn A",
    "email": "test@example.com",
    "phoneNumber": "0123456789",
    "role": "CUSTOMER"
    // ❌ Thiếu thông tin session
  }
}
```

**✅ Cải thiện Response**:
```java
// DTO mới
@Data
@Builder
public class ResponseLogin {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private UserRole role;  // ✅ Dùng enum
    
    // ✅ Thêm session info
    private String sessionId;
    private LocalDateTime sessionExpiresAt;
    private boolean rememberMe;
}

// Controller
@PostMapping("/login")
public ResponseEntity<ApiResponse<?>> login(
        @Valid @RequestBody RequestLogin request,
        @RequestParam(required = false) Boolean rememberMe,  // ✅ Support remember-me
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
) {
    ResponseLogin response = authService.login(request, rememberMe, httpRequest, httpResponse);
    
    ApiResponse<ResponseLogin> apiResponse = ApiResponse.<ResponseLogin>builder()
        .success(true)
        .message("Đăng nhập thành công")
        .data(response)
        .build();
    
    return ResponseEntity.ok(apiResponse);
}

// Service
public ResponseLogin login(
        RequestLogin request,
        Boolean rememberMe,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
) {
    // ... authentication logic ...
    
    // ✅ Cập nhật online status
    user.setOnline(true);
    userRepository.save(user);
    
    // ✅ Lấy session info
    HttpSession session = httpRequest.getSession();
    LocalDateTime expiresAt = LocalDateTime.now()
        .plusSeconds(session.getMaxInactiveInterval());
    
    return ResponseLogin.builder()
        .id(user.getId())
        .fullName(user.getFullName())
        .email(user.getEmail())
        .phoneNumber(user.getPhoneNumber())
        .role(user.getRole())
        .sessionId(session.getId())
        .sessionExpiresAt(expiresAt)
        .rememberMe(rememberMe != null && rememberMe)
        .build();
}
```

---

### 🟢 LOW - Tối Ưu Hóa

#### 7. **Thêm Audit Trail**

```java
@Entity
@Table(name = "user_login_history")
public class UserLoginHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;
    private LocalDateTime loginAt;
    private String ipAddress;
    private String userAgent;
    private boolean success;
    private String failureReason;
}
```

#### 8. **Thêm Password Policy**

```java
@NotBlank(message = "Mật khẩu không được để trống")
@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
    message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt"
)
private String password;
```

#### 9. **Thêm Rate Limiting**

```java
// Sử dụng Bucket4j hoặc Resilience4j
@RateLimiter(name = "login")
@PostMapping("/login")
public ResponseEntity<ApiResponse<?>> login(...) {
    // ...
}
```

---

## 📋 Checklist Hành Động

### Ưu Tiên 1 (Làm Ngay) ⚡
- [ ] Sửa User entity để dùng `@Enumerated` cho role và status
- [ ] Sửa SecurityConfig từ `hasRole()` sang `hasAuthority()`
- [ ] Cải thiện OTP security (hash + expiry + rate limit)
- [ ] Kiểm tra user status trong CustomUserDetailsService
- [ ] Migration database cho OTP fields mới

### Ưu Tiên 2 (Tuần Này) 📅
- [ ] Thêm session configuration (timeout, concurrent control)
- [ ] Cải thiện logout để clear cookies và session
- [ ] Thêm session info vào login response
- [ ] Cập nhật is_online status khi login/logout
- [ ] Test kỹ phân quyền với Postman

### Ưu Tiên 3 (Tháng Này) 📆
- [ ] Implement remember-me functionality
- [ ] Thêm user login history
- [ ] Thêm password policy validation
- [ ] Implement rate limiting cho login và forgot-password
- [ ] Viết unit tests cho AuthService
- [ ] Thêm integration tests cho security

---

## 🎯 Kết Luận

### Về Cookie và Session
✅ **Cách tiếp cận hiện tại của bạn là ĐÚNG**:
- Dùng session-based authentication với cookie là phù hợp
- Session được tự động lưu trong cookie `JSESSIONID`
- Khi logout, Spring Security tự động xóa cookie và invalidate session
- **Không cần thay đổi cơ chế này**

### Về Role Management
⚠️ **Cần cải thiện**:
- Chuyển từ `String role` sang `UserRole role` với `@Enumerated`
- Sửa SecurityConfig để dùng `hasAuthority()` thay vì `hasRole()`
- Thêm kiểm tra status trong authentication

### Về OTP Security
🔴 **Cần khắc phục ngay**:
- Hash OTP trước khi lưu database
- Thêm expiry time (10-15 phút)
- Giới hạn số lần thử (5 lần)
- Rate limiting cho forgot-password endpoint

---

## 📚 Tài Liệu Tham Khảo

1. [Spring Security Official Docs](https://spring.io/projects/spring-security)
2. [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
3. [Spring Session Management](https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html)
4. [BCrypt Best Practices](https://www.baeldung.com/spring-security-registration-password-encoding-bcrypt)

---

**Tạo bởi**: Kiro AI  
**Ngày**: 2026-09-26  
**Phiên bản**: 1.0

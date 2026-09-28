# TÓM TẮT DỰ ÁN BADMINTON MANAGEMENT SYSTEM

## 🎯 Tổng Quan Dự Án

**Tên dự án**: Badminton Management System  
**Công nghệ**: Spring Boot 4.1.1, Java 21  
**Mục đích**: Hệ thống quản lý sân cầu lông với các chức năng đặt sân, quản lý người dùng, thanh toán và dịch vụ

---

## 📦 Tech Stack

### Core Technologies
- **Spring Boot**: 4.1.1
- **Java**: 21
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA
- **Migration**: Flyway
- **Security**: Spring Security (Session-based)
- **Email**: Spring Mail
- **Build Tool**: Maven

### Key Dependencies
- Lombok (annotation processing)
- Spring Validation
- Spring Dotenv (environment management)
- BCrypt (password encoding)

---

## 🗄️ Cấu Trúc Database

### 1. **USERS** (Người dùng)
- Quản lý thông tin người dùng: khách hàng, nhân viên, quản lý
- **Roles**: MANAGER, STAFF, CUSTOMER
- **Status**: ACTIVE, BLOCKED
- Các trường: id, full_name, phone_number, email, password_hash, role, status, reset_otp, is_online

### 2. **COURTS** (Sân cầu lông)
- Quản lý các sân cầu lông
- Có phân loại: court_type (loại sân)
- Trạng thái: status
- Có trường image_url để lưu hình ảnh sân

### 3. **PRICE_CONFIGS** (Cấu hình giá sân)
- Bảng giá theo loại sân và khung giờ
- Hỗ trợ giá giờ cao điểm (is_peak_hour)
- Các trường: court_type, start_time, end_time, price_per_hour

### 4. **DISCOUNT_CONFIGS** (Cấu hình giảm giá)
- Chiết khấu theo số tháng đặt sân cố định
- Trạng thái: ACTIVE
- Các trường: min_months, discount_percent

### 5. **RECURRING_BOOKINGS** (Đặt sân cố định)
- Gói đặt sân dài hạn theo ngày trong tuần
- Tự động áp dụng chiết khấu
- Liên kết: customer_id, court_id
- Trạng thái: ACTIVE
- Các trường: start_date, end_date, days_of_week, start_time, end_time, discount_rate, total_amount

### 6. **BOOKINGS** (Lịch đặt sân)
- Lịch đặt sân đơn lẻ hoặc từ gói cố định
- Có mã booking_code duy nhất
- **Booking Status**: PENDING, CONFIRMED, CHECKED_IN, CANCELLED, COMPLETED
- **Payment Status**: UNPAID, PAID, REFUNDED
- Liên kết: customer_id, court_id, recurring_booking_id, created_by_staff_id, paid_by_staff_id
- Có expire_at để hủy booking chưa thanh toán
- Các trường giá: court_price, service_price, total_amount

### 7. **SERVICES** (Dịch vụ)
- Các dịch vụ đi kèm (nước uống, cho thuê vợt, v.v.)
- Phân loại: category
- Trạng thái: ACTIVE
- Các trường: name, unit_price

### 8. **BOOKING_SERVICES** (Dịch vụ trong booking)
- Liên kết giữa booking và services
- Lưu giá tại thời điểm đặt
- Các trường: booking_id, service_id, quantity, price_at_booking, subtotal

### 9. **FAQS** (Câu hỏi thường gặp)
- Quản lý câu hỏi và trả lời
- Phân loại theo category
- Theo dõi người cập nhật: updated_by

### Index Strategy
- Tối ưu query theo court_id, booking_date, status
- Index cho recurring bookings lookup
- Index cho payment và expire tracking

---

## 🔐 Authentication & Authorization

### Phân Quyền
1. **PUBLIC**: 
   - `/auth/register` - Đăng ký
   - `/auth/login` - Đăng nhập
   - `/auth/forgot-password` - Quên mật khẩu
   - `/auth/reset-password` - Đặt lại mật khẩu
   - `/auth/logout` - Đăng xuất

2. **MANAGER**: Quyền cao nhất, quản lý toàn bộ hệ thống

3. **STAFF**: Có quyền như MANAGER + xử lý các nghiệp vụ hàng ngày

4. **CUSTOMER**: Quyền cơ bản nhất, đặt sân và quản lý booking của mình

### Security Features
- **Authentication**: Session-based authentication
- **Password**: BCrypt encoding
- **CSRF**: Disabled (API)
- **OTP**: 6-digit OTP cho reset password (lưu trong DB)

---

## 📡 API Endpoints (Đã Triển Khai)

### 🔓 Authentication APIs

#### 1. POST `/auth/register`
**Mô tả**: Đăng ký tài khoản khách hàng mới

**Request Body**:
```json
{
  "fullName": "Nguyễn Văn A",
  "phoneNumber": "0123456789",
  "email": "test@example.com",
  "password": "123456"
}
```

**Validation**:
- fullName: không được trống
- phoneNumber: 10 số, bắt đầu bằng 0
- email: đúng định dạng email, duy nhất
- password: tối thiểu 6 ký tự

**Response Success** (201):
```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "id": 1,
    "fullName": "Nguyễn Văn A",
    "phoneNumber": "0123456789",
    "email": "test@example.com",
    "role": "CUSTOMER",
    "status": "ACTIVE"
  }
}
```

**Response Error** (400):
```json
{
  "success": false,
  "message": "Email đã tồn tại",
  "data": null
}
```

---

#### 2. POST `/auth/login`
**Mô tả**: Đăng nhập bằng email hoặc số điện thoại

**Request Body**:
```json
{
  "login": "test@example.com",
  "password": "123456"
}
```

**Validation**:
- login: không được trống (có thể là email hoặc số điện thoại)
- password: không được trống

**Response Success** (200):
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
  }
}
```

**Response Error** (400):
```json
{
  "success": false,
  "message": "Tài khoản hoặc mật khẩu không đúng",
  "data": null
}
```
hoặc
```json
{
  "success": false,
  "message": "Tài khoản đã bị khóa",
  "data": null
}
```

**Note**: Session được tạo tự động sau khi đăng nhập thành công

---

#### 3. POST `/auth/forgot-password`
**Mô tả**: Gửi mã OTP để reset mật khẩu

**Request Body**:
```json
{
  "email": "test@example.com"
}
```

**Validation**:
- email: không được trống, đúng định dạng

**Response Success** (200):
```json
{
  "success": true,
  "message": "Gửi yêu cầu đổi mật khẩu thành công",
  "data": null
}
```

**Response Error** (400):
```json
{
  "success": false,
  "message": "Email không tồn tại",
  "data": null
}
```

**Note**: 
- OTP gồm 6 chữ số được gửi qua email
- OTP được lưu vào trường `reset_otp` trong database

---

#### 4. POST `/auth/reset-password`
**Mô tả**: Đặt lại mật khẩu với OTP

**Request Body**:
```json
{
  "email": "test@example.com",
  "otp": "123456",
  "newPassword": "newpass123",
  "confirmPassword": "newpass123"
}
```

**Validation**:
- email: không được trống, đúng định dạng
- otp: đúng 6 chữ số
- newPassword: tối thiểu 6 ký tự
- confirmPassword: không được trống

**Response Success** (200):
```json
{
  "success": true,
  "message": "Đổi mật khẩu thành công",
  "data": null
}
```

**Response Error** (400):
```json
{
  "success": false,
  "message": "Mã otp không hợp lệ",
  "data": null
}
```
hoặc
```json
{
  "success": false,
  "message": "Mật khẩu xác nhận không khớp",
  "data": null
}
```

**Note**: Sau khi reset thành công, OTP sẽ bị xóa khỏi database

---

#### 5. POST `/auth/logout`
**Mô tả**: Đăng xuất khỏi hệ thống

**Response Success** (200):
- Status: 200 OK
- Session được xóa

---

## 🏗️ Cấu Trúc Dự Án

```
src/main/java/com/bteam/badmintonmanagement/
├── config/                          # Cấu hình
│   ├── SecurityConfig.java         # Cấu hình bảo mật
│   └── CustomUserDetails.java      # Custom User Details
├── controller/                      # Controllers
│   └── AuthController.java         # Authentication endpoints
├── dto/                            # Data Transfer Objects
│   ├── request/
│   │   ├── RequestRegister.java
│   │   ├── RequestLogin.java
│   │   ├── RequestForgotPassword.java
│   │   └── RequestResetPassword.java
│   └── response/
│       ├── ApiResponse.java        # Chuẩn response chung
│       ├── ResponseRegister.java
│       └── ResponseLogin.java
├── entity/                         # Entities
│   └── user/
│       ├── User.java              # User entity
│       ├── UserRole.java          # Enum: MANAGER, STAFF, CUSTOMER
│       └── UserStatus.java        # Enum: ACTIVE, BLOCKED
├── exception/                      # Exception handling
│   ├── GlobalExceptionHandler.java
│   └── InvalidDataException.java
├── repository/                     # Repositories
│   └── UserRepository.java
├── service/                        # Business logic
│   ├── AuthService.java
│   ├── CustomUserDetailsService.java
│   └── EmailService.java
└── BadmintonManagementApplication.java
```

```
src/main/resources/
├── application.properties          # Cấu hình ứng dụng
└── db/migration/                   # Flyway migrations
    ├── V1__Create_DB.sql          # Tạo database ban đầu
    ├── V2__Alter_DB.sql           # Thêm image_url, reset_otp
    └── V3__Add.sql                # Thêm is_online
```

---

## 🔧 Configuration

### Environment Variables (.env)
```properties
SERVER_PORT=8080

# Database
DB_URL=jdbc:postgresql://localhost:5432/badminton_db
DB_USERNAME=your_username
DB_PASSWORD=your_password

# Mail
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true
```

---

## ⚠️ Exception Handling

### 1. InvalidDataException
- Xử lý các lỗi nghiệp vụ (email đã tồn tại, OTP sai, v.v.)
- HTTP Status: 400 Bad Request

### 2. MethodArgumentNotValidException
- Xử lý validation errors từ @Valid
- Trả về map các field lỗi
- HTTP Status: 400 Bad Request

### Response Format Lỗi
```json
{
  "success": false,
  "message": "Dữ liệu không hợp lệ",
  "data": {
    "email": "Email không đúng định dạng",
    "password": "Mật khẩu phải có ít nhất 6 ký tự"
  }
}
```

---

## 📋 Business Logic Flow

### 1. Đăng Ký
1. Validate input data
2. Kiểm tra email/phone đã tồn tại chưa
3. Mã hóa mật khẩu bằng BCrypt
4. Tạo user với role CUSTOMER và status ACTIVE
5. Lưu vào database
6. Trả về thông tin user (không bao gồm password)

### 2. Đăng Nhập
1. Validate input
2. Xác thực với AuthenticationManager
3. Kiểm tra account status (BLOCKED → reject)
4. Tìm user theo email/phone
5. Tạo SecurityContext và lưu session
6. Trả về thông tin user

### 3. Quên Mật Khẩu
1. Validate email
2. Kiểm tra email có tồn tại không
3. Tạo OTP 6 chữ số ngẫu nhiên
4. Lưu OTP vào database (trường reset_otp)
5. Gửi OTP qua email

### 4. Reset Mật Khẩu
1. Validate input (email, OTP, passwords)
2. Kiểm tra email tồn tại
3. So khớp newPassword với confirmPassword
4. Verify OTP từ database
5. Mã hóa mật khẩu mới
6. Xóa OTP khỏi database
7. Lưu password mới

---

## 🚀 Các Tính Năng Chưa Triển Khai (Dựa Trên Schema)

### Courts Management
- CRUD operations cho sân cầu lông
- Upload và quản lý hình ảnh sân
- Quản lý trạng thái sân

### Price Configuration
- Cấu hình giá theo loại sân và khung giờ
- Quản lý giá giờ cao điểm

### Discount Management
- Cấu hình chiết khấu theo số tháng

### Recurring Bookings (Đặt Sân Cố Định)
- Đặt sân định kỳ theo ngày trong tuần
- Tự động tính toán chiết khấu
- Quản lý gói đặt sân dài hạn

### Regular Bookings (Đặt Sân Thường)
- Đặt sân theo giờ
- Quản lý trạng thái booking (PENDING → CONFIRMED → CHECKED_IN → COMPLETED)
- Tự động hủy booking hết hạn chưa thanh toán
- Tính toán giá dựa trên price_configs

### Payment Management
- Xử lý thanh toán
- Quản lý phương thức thanh toán
- Tracking paid_by_staff_id

### Services Management
- CRUD operations cho dịch vụ
- Thêm dịch vụ vào booking
- Tính toán tổng tiền bao gồm dịch vụ

### FAQ Management
- CRUD operations cho FAQ
- Phân loại FAQ theo category

### User Management
- CRUD operations cho user (MANAGER/STAFF)
- Block/Unblock user
- Tracking online status

### Reporting & Analytics
- Báo cáo doanh thu
- Thống kê booking
- Phân tích hiệu suất sân

---

## 📊 Database Relationships

```
users (1) -----> (*) bookings (customer_id)
users (1) -----> (*) bookings (created_by_staff_id)
users (1) -----> (*) bookings (paid_by_staff_id)
users (1) -----> (*) recurring_bookings (customer_id)
users (1) -----> (*) faqs (updated_by)

courts (1) -----> (*) bookings (court_id)
courts (1) -----> (*) recurring_bookings (court_id)

recurring_bookings (1) -----> (*) bookings (recurring_booking_id)

bookings (1) -----> (*) booking_services (booking_id)
services (1) -----> (*) booking_services (service_id)
```

---

## 🎨 Response Format Chuẩn

Tất cả API đều sử dụng format response thống nhất:

```json
{
  "success": true/false,
  "message": "Thông báo",
  "data": {} hoặc null
}
```

---

## 🔒 Security Notes

1. **Password**: Sử dụng BCrypt với default strength
2. **Session**: HttpSession-based authentication (không dùng JWT)
3. **CSRF**: Disabled (phù hợp cho API)
4. **OTP**: 6 chữ số, lưu plaintext trong DB (có thể cải thiện bằng cách hash hoặc set expiry time)
5. **Email/Phone**: Tự động normalize (trim, lowercase cho email)

---

## 📝 Notes & Recommendations

### Đã Làm Tốt ✅
- Cấu trúc database rõ ràng với indexes hợp lý
- Exception handling tập trung
- Validation đầy đủ
- Response format nhất quán
- Email service cho OTP

### Cần Cải Thiện 🔧
1. **OTP Security**:
   - Thêm expiry time cho OTP
   - Hash OTP thay vì lưu plaintext
   - Rate limiting cho forgot-password

2. **Session Management**:
   - Thêm session timeout config
   - Implement remember-me

3. **Logging**:
   - Thêm logging cho các action quan trọng
   - Audit trail

4. **Testing**:
   - Unit tests cho services
   - Integration tests cho APIs

5. **Documentation**:
   - Swagger/OpenAPI documentation
   - Postman collection

6. **Booking Expiry**:
   - Scheduled job để tự động hủy booking hết hạn

---

## 🚀 Cách Chạy Dự Án

1. **Setup Database**:
   - Tạo PostgreSQL database
   - Cấu hình .env file

2. **Build & Run**:
```bash
./mvnw clean install
./mvnw spring-boot:run
```

3. **Flyway Migration**:
   - Tự động chạy khi start application
   - Tạo tất cả tables và indexes

4. **Test API**:
   - Server chạy tại: `http://localhost:8080`
   - Test với Postman hoặc curl

---

## 📞 Contact & Support

**Team**: BTeam  
**Version**: 0.0.1-SNAPSHOT  
**Java Version**: 21  
**Spring Boot Version**: 4.1.1

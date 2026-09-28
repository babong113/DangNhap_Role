# 📋 KẾ HOẠCH PHÁT TRIỂN TÍNH NĂNG ĐẶT SÂN CỐ ĐỊNH

## 🎯 Tổng Quan

**Mục tiêu**: Triển khai tính năng đặt sân cố định (recurring booking) cho hệ thống quản lý sân cầu lông

**2 Màn hình cần phát triển**:
1. **Màn hình 1**: Danh sách sân - Tìm kiếm và lọc sân có sẵn
2. **Màn hình 2**: Chọn khung giờ và đặt sân cố định

---

## 🖼️ PHÂN TÍCH MÀN HÌNH 1: DANH SÁCH SÂN

### UI Components

#### Header Section
- **Title**: "Danh sách sân"
- **Subtitle**: "Chọn ngày phù hợp và tìm sân còn trống dành cho bạn."
- **Badge**: "03 Sân hiện có" (số lượng động)

#### Filter Section
1. **Ngày chơi**: Date picker (mặc định ngày hiện tại)
2. **Loại sân**: Dropdown select
   - Tất cả loại sân
   - Sân tiêu chuẩn
   - Sân VIP
   - (có thể có thêm loại khác)
3. **Button**: "Tìm sân trống" - Trigger search

#### Court Cards Section
Hiển thị danh sách sân dạng card grid với thông tin:

**Card 1 - Sân 01**:
- Label: "Sân tiêu chuẩn"
- Tên: "Sân 01"
- Status badge: "Hoạt động" (màu xanh)
- Mô tả: "Sân tiêu chuẩn trong nhà"
- Tiện ích: ✓ Thiết bị PVC, ✓ Điều hòa
- Giá: "100,000đ/giờ"
- Button: "Xem lịch" (màu tím)

**Card 2 - Sân VIP 02**:
- Label: "Sân VIP"
- Tên: "Sân VIP 02"
- Status badge: "Hoạt động" (màu xanh)
- Mô tả: "Sân VIP rộng rãi, ánh sáng chuẩn"
- Tiện ích: ✓ Thiện cao cấp, ✓ Phòng thầy
- Giá: "250,000đ/giờ"
- Button: "Xem lịch" (màu tím)

**Card 3 - Sân 03**:
- Label: "Sân tiêu chuẩn"
- Tên: "Sân 03"
- Status badge: "Bảo trì" (màu cam/đỏ)
- Mô tả: "Sân tiêu chuẩn trong nhà"
- Tiện ích: ✓ Đang nâng cấp
- Giá: "80,000đ/giờ"
- Button: "Tạm đóng" (disabled, màu xám)

### Business Logic

1. **Tìm kiếm sân**:
   - Lọc theo ngày đặt
   - Lọc theo loại sân
   - Chỉ hiển thị sân "Hoạt động" có thể đặt được
   - Sân "Bảo trì" hiển thị nhưng disable

2. **Hiển thị thông tin**:
   - Tên sân
   - Loại sân (court_type)
   - Status (ACTIVE/MAINTENANCE)
   - Giá cơ bản theo loại sân
   - Tiện ích/mô tả

3. **Navigate to detail**:
   - Click "Xem lịch" → Chuyển sang màn hình 2
   - Truyền: courtId, ngày đã chọn

---

## 🖼️ PHÂN TÍCH MÀN HÌNH 2: CHỌN KHUNG GIỜ

### UI Components

#### Left Panel - Court Info Card
- **Tương tự Card từ màn hình 1**
- Hiển thị thông tin sân đã chọn
- Giá hiển thị: "100,000đ/giờ (Từ 06:00 - 08:00)"

#### Right Panel - Time Slot Selection

##### Date Picker
- **Ngày đặt sân**: Date picker (hiển thị ngày đã chọn từ màn hình 1)

##### Chọn Khung Giờ
- **Label**: "Chọn khung giờ" + text hướng dẫn "Tối đa 3 giờ mỗi lần đặt"

##### Time Slots Grid
**Layout**: Grid 4-5 cột, nhiều hàng hiển thị các time slots 30 phút

**Time slots từ 06:00 → 23:30** (mỗi slot 30 phút):
- **Đã đặt** (màu xám, disabled): 17:30, 18:00
- **Còn trống** (màu trắng, có thể chọn): 06:00, 06:30, 07:00, ...
- **Đang chọn** (màu xanh lá): 18:30, 19:00, 19:30

##### Legend
- 🟦 Đã đặt (không chọn được)
- ⬜ Còn trống (có thể chọn)
- 🟩 Đang chọn (user đang select)

##### Summary Section
- **Text**: "Đã chọn: 18:30 - 20:00 (3 khung - 1.5 giờ)"
- **Tổng tiền**: "125,000đ" (calculated)

##### Action Button
- **Button**: "Đặt sân ngay →" (màu tím)

### Business Logic

#### 1. Load Availability
- Load các time slots đã được đặt cho sân trong ngày
- Kiểm tra:
  - Bookings thường (table: bookings)
  - Recurring bookings (table: recurring_bookings) - check days_of_week
- Mark các slot đã đặt là disabled

#### 2. Time Slot Selection
- **Cho phép chọn nhiều slot liên tiếp**
- **Giới hạn**: Tối đa 3 giờ (6 time slots 30 phút) - theo text trên UI
- **Validation**:
  - Không cho chọn slot đã đặt
  - Chỉ cho chọn các slot liên tiếp (không có gap)
  - Tự động highlight range khi chọn

#### 3. Price Calculation
- Lấy giá từ `price_config` table
- **Tính theo**:
  - Loại sân (court_type)
  - Khung giờ (start_time, end_time)
  - Peak hour pricing (is_peak_hour)
- **Formula**:
  ```
  Tổng tiền = Σ (price_per_hour * duration_in_hours) cho từng slot
  ```

#### 4. Tạo Recurring Booking
- **Trigger**: Click "Đặt sân ngay"
- **Navigate to**: Form đặt sân cố định
  - Chọn ngày bắt đầu / kết thúc
  - Chọn các ngày trong tuần (T2-CN)
  - Xem trước lịch
  - Tính tổng tiền + discount
  - Xác nhận và thanh toán

---

## 📊 DATABASE TABLES LIÊN QUAN

### 1. COURTS (Bảng Sân)
```sql
CREATE TABLE courts (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,           -- "Sân 01", "Sân VIP 02"
    court_type VARCHAR(30) NOT NULL,     -- "STANDARD", "VIP", "PREMIUM"
    status VARCHAR(20) NOT NULL,         -- "ACTIVE", "MAINTENANCE", "INACTIVE"
    image_url VARCHAR(500),              -- Link hình ảnh sân
    description TEXT,                    -- Mô tả sân
    facilities TEXT                      -- JSON: ["Thiết bị PVC", "Điều hòa"]
);
```

**Note**: Cần thêm các trường `description`, `facilities`

### 2. PRICE_CONFIGS (Bảng Giá)
```sql
CREATE TABLE price_configs (
    id BIGINT PRIMARY KEY,
    court_type VARCHAR(30) NOT NULL,     -- "STANDARD", "VIP"
    start_time TIME NOT NULL,            -- 06:00
    end_time TIME NOT NULL,              -- 12:00
    price_per_hour NUMERIC(12,2),        -- 100000
    is_peak_hour BOOLEAN DEFAULT FALSE
);
```

**Ví dụ data**:
| court_type | start_time | end_time | price_per_hour | is_peak_hour |
|------------|------------|----------|----------------|--------------|
| STANDARD   | 06:00      | 12:00    | 100,000        | false        |
| STANDARD   | 12:00      | 17:00    | 120,000        | false        |
| STANDARD   | 17:00      | 22:00    | 150,000        | true         |
| VIP        | 06:00      | 12:00    | 200,000        | false        |
| VIP        | 12:00      | 22:00    | 250,000        | true         |

### 3. RECURRING_BOOKINGS (Đặt Sân Cố Định)
```sql
CREATE TABLE recurring_bookings (
    id BIGINT PRIMARY KEY,
    customer_id BIGINT,
    court_id BIGINT,
    start_date DATE NOT NULL,            -- Ngày bắt đầu gói
    end_date DATE NOT NULL,              -- Ngày kết thúc gói
    days_of_week VARCHAR(50),            -- "MONDAY,WEDNESDAY,FRIDAY"
    start_time TIME NOT NULL,            -- 18:30
    end_time TIME NOT NULL,              -- 20:00
    discount_rate NUMERIC(5,2),          -- 10.00 (%)
    total_amount NUMERIC(12,2),          -- Tổng tiền sau discount
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, CANCELLED, COMPLETED
    created_at TIMESTAMP
);
```

### 4. BOOKINGS (Lịch Đặt Đơn Lẻ)
```sql
-- Đã có trong V1__Create_DB.sql
-- Dùng để check availability
```

### 5. DISCOUNT_CONFIGS (Cấu Hình Giảm Giá)
```sql
CREATE TABLE discount_configs (
    id BIGINT PRIMARY KEY,
    min_months INT NOT NULL,             -- 1, 3, 6, 12
    discount_percent NUMERIC(5,2),       -- 5.00, 10.00, 15.00, 20.00
    status VARCHAR(20) DEFAULT 'ACTIVE'
);
```

**Ví dụ data**:
| min_months | discount_percent | description      |
|------------|------------------|------------------|
| 1          | 5.00             | Đặt 1 tháng -5%  |
| 3          | 10.00            | Đặt 3 tháng -10% |
| 6          | 15.00            | Đặt 6 tháng -15% |
| 12         | 20.00            | Đặt 1 năm -20%   |

---

## 🔌 API ENDPOINTS SPECIFICATION

### 📌 GROUP 1: COURTS MANAGEMENT

#### 1.1. GET `/api/courts`
**Mục đích**: Lấy danh sách tất cả sân (cho màn hình 1)

**Query Parameters**:
```
- date: String (optional) - Format: "yyyy-MM-dd"
  → Dùng để filter sân có slot trống trong ngày
- courtType: String (optional) - "ALL", "STANDARD", "VIP", "PREMIUM"
- status: String (optional) - "ACTIVE", "MAINTENANCE", "ALL"
- page: int (default: 0)
- size: int (default: 10)
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Lấy danh sách sân thành công",
  "data": {
    "courts": [
      {
        "id": 1,
        "name": "Sân 01",
        "courtType": "STANDARD",
        "status": "ACTIVE",
        "description": "Sân tiêu chuẩn trong nhà",
        "imageUrl": "https://example.com/court1.jpg",
        "facilities": [
          "Thiết bị PVC",
          "Điều hòa"
        ],
        "basePrice": 100000,
        "priceRange": {
          "min": 100000,
          "max": 150000
        }
      },
      {
        "id": 2,
        "name": "Sân VIP 02",
        "courtType": "VIP",
        "status": "ACTIVE",
        "description": "Sân VIP rộng rãi, ánh sáng chuẩn",
        "imageUrl": "https://example.com/court2.jpg",
        "facilities": [
          "Thiết bị cao cấp",
          "Phòng thay đồ"
        ],
        "basePrice": 200000,
        "priceRange": {
          "min": 200000,
          "max": 250000
        }
      },
      {
        "id": 3,
        "name": "Sân 03",
        "courtType": "STANDARD",
        "status": "MAINTENANCE",
        "description": "Sân tiêu chuẩn trong nhà",
        "imageUrl": "https://example.com/court3.jpg",
        "facilities": [
          "Đang nâng cấp"
        ],
        "basePrice": 80000,
        "priceRange": {
          "min": 80000,
          "max": 120000
        }
      }
    ],
    "totalCount": 3,
    "currentPage": 0,
    "totalPages": 1
  }
}
```

**Business Logic**:
1. Query courts từ database
2. Filter theo courtType nếu có
3. Filter theo status (mặc định chỉ lấy ACTIVE)
4. Nếu có `date` parameter:
   - Check availability cho ngày đó
   - Có thể thêm flag `hasAvailability: true/false`
5. Lấy price range từ price_configs theo court_type
6. Pagination

**Security**: PUBLIC hoặc CUSTOMER+ (tùy business rule)

---

#### 1.2. GET `/api/courts/{courtId}`
**Mục đích**: Lấy chi tiết một sân cụ thể

**Path Variable**:
- `courtId`: Long

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Lấy thông tin sân thành công",
  "data": {
    "id": 1,
    "name": "Sân 01",
    "courtType": "STANDARD",
    "status": "ACTIVE",
    "description": "Sân tiêu chuẩn trong nhà với đầy đủ tiện nghi",
    "imageUrl": "https://example.com/court1.jpg",
    "facilities": [
      "Thiết bị PVC chất lượng cao",
      "Điều hòa 2 chiều",
      "Ánh sáng LED chuẩn thi đấu",
      "Phòng thay đồ"
    ],
    "pricing": [
      {
        "startTime": "06:00",
        "endTime": "12:00",
        "pricePerHour": 100000,
        "isPeakHour": false
      },
      {
        "startTime": "12:00",
        "endTime": "17:00",
        "pricePerHour": 120000,
        "isPeakHour": false
      },
      {
        "startTime": "17:00",
        "endTime": "22:00",
        "pricePerHour": 150000,
        "isPeakHour": true
      }
    ]
  }
}
```

**Business Logic**:
1. Tìm court theo ID
2. Load thông tin chi tiết
3. Load pricing từ price_configs theo court_type

**Security**: PUBLIC hoặc CUSTOMER+

---

### 📌 GROUP 2: COURT AVAILABILITY

#### 2.1. GET `/api/courts/{courtId}/availability`
**Mục đích**: Kiểm tra khung giờ trống của sân trong một ngày (cho màn hình 2)

**Path Variable**:
- `courtId`: Long

**Query Parameters**:
```
- date: String (required) - Format: "yyyy-MM-dd"
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Lấy thông tin khung giờ thành công",
  "data": {
    "courtId": 1,
    "courtName": "Sân 01",
    "date": "2026-09-20",
    "dayOfWeek": "SATURDAY",
    "timeSlots": [
      {
        "startTime": "06:00",
        "endTime": "06:30",
        "status": "AVAILABLE",
        "price": 50000
      },
      {
        "startTime": "06:30",
        "endTime": "07:00",
        "status": "AVAILABLE",
        "price": 50000
      },
      {
        "startTime": "17:30",
        "endTime": "18:00",
        "status": "BOOKED",
        "price": null,
        "bookedBy": "RECURRING"
      },
      {
        "startTime": "18:00",
        "endTime": "18:30",
        "status": "BOOKED",
        "price": null,
        "bookedBy": "REGULAR"
      },
      {
        "startTime": "18:30",
        "endTime": "19:00",
        "status": "AVAILABLE",
        "price": 75000
      }
    ],
    "operatingHours": {
      "open": "06:00",
      "close": "23:30"
    }
  }
}
```

**Business Logic**:
1. Parse date parameter
2. Generate tất cả time slots 30 phút từ 06:00 → 23:30
3. Query bookings cho court + date:
   ```sql
   SELECT * FROM bookings 
   WHERE court_id = ? 
     AND booking_date = ?
     AND booking_status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')
   ```
4. Query recurring_bookings cho court + day_of_week:
   ```sql
   SELECT * FROM recurring_bookings
   WHERE court_id = ?
     AND status = 'ACTIVE'
     AND ? BETWEEN start_date AND end_date
     AND days_of_week LIKE ?
   ```
5. Mark các slots đã book là "BOOKED"
6. Tính price cho mỗi slot từ price_configs
7. Return time slots với status

**Security**: PUBLIC hoặc CUSTOMER+

---

#### 2.2. POST `/api/courts/{courtId}/check-availability`
**Mục đích**: Kiểm tra một range time slot cụ thể có khả dụng không

**Path Variable**:
- `courtId`: Long

**Request Body**:
```json
{
  "date": "2026-09-20",
  "startTime": "18:30",
  "endTime": "20:00",
  "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"],
  "startDate": "2026-09-20",
  "endDate": "2026-12-20"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Khung giờ có thể đặt",
  "data": {
    "available": true,
    "conflicts": [],
    "totalSessions": 36,
    "estimatedPrice": 5400000,
    "priceBreakdown": {
      "basePrice": 6000000,
      "discountPercent": 10.00,
      "discountAmount": 600000,
      "finalPrice": 5400000
    }
  }
}
```

**Response Error (400)** - Có conflict:
```json
{
  "success": false,
  "message": "Khung giờ không khả dụng",
  "data": {
    "available": false,
    "conflicts": [
      {
        "date": "2026-09-23",
        "startTime": "18:30",
        "endTime": "19:00",
        "reason": "Đã có booking cố định"
      },
      {
        "date": "2026-09-25",
        "startTime": "19:00",
        "endTime": "20:00",
        "reason": "Đã có booking thường"
      }
    ]
  }
}
```

**Business Logic**:
1. Parse input
2. Generate danh sách các ngày cần check (based on daysOfWeek)
3. For each date, check conflicts với:
   - Regular bookings
   - Recurring bookings
4. Tính số sessions và giá:
   - Count số lần chơi = số tuần * số ngày/tuần
   - Tính giá từ price_configs
   - Apply discount từ discount_configs (based on số tháng)
5. Return availability status + conflicts

**Security**: CUSTOMER+

---

### 📌 GROUP 3: RECURRING BOOKINGS

#### 3.1. POST `/api/recurring-bookings`
**Mục đích**: Tạo đặt sân cố định mới

**Request Body**:
```json
{
  "courtId": 1,
  "startDate": "2026-09-20",
  "endDate": "2026-12-20",
  "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"],
  "startTime": "18:30",
  "endTime": "20:00"
}
```

**Validation**:
- startDate < endDate
- daysOfWeek không rỗng, valid values: MONDAY-SUNDAY
- startTime < endTime
- Court phải ACTIVE
- Không conflict với bookings khác
- User phải là CUSTOMER+

**Response Success (201)**:
```json
{
  "success": true,
  "message": "Đặt sân cố định thành công",
  "data": {
    "recurringBookingId": 15,
    "courtId": 1,
    "courtName": "Sân 01",
    "startDate": "2026-09-20",
    "endDate": "2026-12-20",
    "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"],
    "startTime": "18:30",
    "endTime": "20:00",
    "duration": "1.5 giờ",
    "totalSessions": 36,
    "pricePerSession": 150000,
    "subtotal": 5400000,
    "discountRate": 10.00,
    "discountAmount": 540000,
    "totalAmount": 4860000,
    "status": "ACTIVE",
    "createdAt": "2026-09-20T10:30:00"
  }
}
```

**Business Logic**:
1. **Validate input**:
   - Date range hợp lệ
   - Court tồn tại và ACTIVE
   - Time slots hợp lệ
   
2. **Check availability**:
   - Gọi logic từ endpoint 2.2
   - Nếu có conflict → reject
   
3. **Calculate pricing**:
   - Tính số sessions (số tuần * số ngày/tuần * số giờ)
   - Lấy giá từ price_configs
   - Tính discount từ discount_configs
   - Calculate final price
   
4. **Create recurring booking**:
   - Insert vào recurring_bookings table
   - Status = ACTIVE
   
5. **Generate individual bookings** (optional - có thể làm sau):
   - Tạo records trong bookings table cho mỗi session
   - Link với recurring_booking_id
   - Giúp dễ quản lý và check-in
   
6. **Payment** (tuỳ business flow):
   - Option A: Redirect to payment
   - Option B: Đặt trước, payment_status = UNPAID

**Security**: CUSTOMER+

---

#### 3.2. GET `/api/recurring-bookings`
**Mục đích**: Lấy danh sách đặt sân cố định (của customer hoặc tất cả nếu staff)

**Query Parameters**:
```
- customerId: Long (optional - STAFF dùng để filter)
- status: String (optional) - "ACTIVE", "CANCELLED", "COMPLETED", "ALL"
- page: int
- size: int
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Lấy danh sách đặt sân cố định thành công",
  "data": {
    "recurringBookings": [
      {
        "id": 15,
        "customer": {
          "id": 10,
          "fullName": "Nguyễn Văn A",
          "phoneNumber": "0123456789"
        },
        "court": {
          "id": 1,
          "name": "Sân 01",
          "courtType": "STANDARD"
        },
        "startDate": "2026-09-20",
        "endDate": "2026-12-20",
        "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"],
        "startTime": "18:30",
        "endTime": "20:00",
        "totalSessions": 36,
        "completedSessions": 12,
        "remainingSessions": 24,
        "totalAmount": 4860000,
        "status": "ACTIVE",
        "createdAt": "2026-09-20T10:30:00"
      }
    ],
    "totalCount": 1,
    "currentPage": 0,
    "totalPages": 1
  }
}
```

**Business Logic**:
1. Nếu role = CUSTOMER: auto filter by current user
2. Nếu role = STAFF/MANAGER: có thể xem tất cả hoặc filter by customerId
3. Filter by status
4. Join với users, courts
5. Có thể tính completed/remaining sessions từ bookings table
6. Pagination

**Security**: CUSTOMER (own), STAFF+ (all)

---

#### 3.3. GET `/api/recurring-bookings/{id}`
**Mục đích**: Xem chi tiết một recurring booking

**Path Variable**:
- `id`: Long

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Lấy thông tin đặt sân cố định thành công",
  "data": {
    "id": 15,
    "customer": {
      "id": 10,
      "fullName": "Nguyễn Văn A",
      "email": "test@example.com",
      "phoneNumber": "0123456789"
    },
    "court": {
      "id": 1,
      "name": "Sân 01",
      "courtType": "STANDARD",
      "imageUrl": "https://example.com/court1.jpg"
    },
    "schedule": {
      "startDate": "2026-09-20",
      "endDate": "2026-12-20",
      "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"],
      "startTime": "18:30",
      "endTime": "20:00",
      "duration": "1.5 giờ"
    },
    "pricing": {
      "pricePerSession": 150000,
      "totalSessions": 36,
      "subtotal": 5400000,
      "discountRate": 10.00,
      "discountAmount": 540000,
      "totalAmount": 4860000
    },
    "progress": {
      "totalSessions": 36,
      "completedSessions": 12,
      "upcomingSessions": 24,
      "cancelledSessions": 0
    },
    "upcomingDates": [
      "2026-09-23",
      "2026-09-25",
      "2026-09-27"
    ],
    "status": "ACTIVE",
    "createdAt": "2026-09-20T10:30:00"
  }
}
```

**Business Logic**:
1. Tìm recurring booking by ID
2. Check permission (customer chỉ xem của mình)
3. Load full details với joins
4. Calculate progress từ bookings table
5. Generate danh sách upcoming dates

**Security**: CUSTOMER (own), STAFF+ (all)

---

#### 3.4. PUT `/api/recurring-bookings/{id}/cancel`
**Mục đích**: Hủy đặt sân cố định

**Path Variable**:
- `id`: Long

**Request Body**:
```json
{
  "reason": "Lý do hủy (optional)"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Hủy đặt sân cố định thành công",
  "data": {
    "recurringBookingId": 15,
    "status": "CANCELLED",
    "cancelledAt": "2026-10-15T14:30:00",
    "refundInfo": {
      "totalPaid": 4860000,
      "usedSessions": 12,
      "usedAmount": 1620000,
      "refundAmount": 3240000,
      "refundMethod": "Chuyển khoản ngược lại"
    }
  }
}
```

**Business Logic**:
1. Check permission (customer chỉ hủy của mình)
2. Check status (chỉ hủy được nếu ACTIVE)
3. Update status = CANCELLED
4. Cancel tất cả upcoming bookings (status = CANCELLED)
5. Calculate refund:
   - Count completed sessions
   - Refund = totalAmount - (completed * pricePerSession)
6. Process refund (tùy business flow)

**Security**: CUSTOMER (own), STAFF+ (all)

---

### 📌 GROUP 4: PRICE & DISCOUNT CONFIGS

#### 4.1. GET `/api/price-configs`
**Mục đích**: Lấy bảng giá theo loại sân (để hiển thị trên UI)

**Query Parameters**:
```
- courtType: String (optional) - "STANDARD", "VIP", etc.
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Lấy bảng giá thành công",
  "data": {
    "priceConfigs": [
      {
        "id": 1,
        "courtType": "STANDARD",
        "startTime": "06:00",
        "endTime": "12:00",
        "pricePerHour": 100000,
        "isPeakHour": false
      },
      {
        "id": 2,
        "courtType": "STANDARD",
        "startTime": "12:00",
        "endTime": "17:00",
        "pricePerHour": 120000,
        "isPeakHour": false
      },
      {
        "id": 3,
        "courtType": "STANDARD",
        "startTime": "17:00",
        "endTime": "22:00",
        "pricePerHour": 150000,
        "isPeakHour": true
      }
    ]
  }
}
```

**Security**: PUBLIC

---

#### 4.2. GET `/api/discount-configs`
**Mục đích**: Lấy danh sách chiết khấu (để hiển thị khi chọn gói)

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Lấy thông tin chiết khấu thành công",
  "data": {
    "discounts": [
      {
        "id": 1,
        "minMonths": 1,
        "discountPercent": 5.00,
        "description": "Đặt 1 tháng giảm 5%"
      },
      {
        "id": 2,
        "minMonths": 3,
        "discountPercent": 10.00,
        "description": "Đặt 3 tháng giảm 10%"
      },
      {
        "id": 3,
        "minMonths": 6,
        "discountPercent": 15.00,
        "description": "Đặt 6 tháng giảm 15%"
      },
      {
        "id": 4,
        "minMonths": 12,
        "discountPercent": 20.00,
        "description": "Đặt 1 năm giảm 20%"
      }
    ]
  }
}
```

**Security**: PUBLIC

---

## 🏗️ ENTITIES & DTOs CẦN TẠO

### Entities

#### 1. Court Entity Enhancement
```java
@Entity
@Table(name = "courts")
public class Court {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Enumerated(EnumType.STRING)
    private CourtType courtType;  // STANDARD, VIP, PREMIUM
    
    @Enumerated(EnumType.STRING)
    private CourtStatus status;  // ACTIVE, MAINTENANCE, INACTIVE
    
    private String imageUrl;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    // JSON hoặc @ElementCollection
    @Column(columnDefinition = "TEXT")
    private String facilities;  // JSON array
}

public enum CourtType {
    STANDARD, VIP, PREMIUM
}

public enum CourtStatus {
    ACTIVE, MAINTENANCE, INACTIVE
}
```

#### 2. PriceConfig Entity
```java
@Entity
@Table(name = "price_configs")
public class PriceConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private CourtType courtType;
    
    private LocalTime startTime;
    private LocalTime endTime;
    
    private BigDecimal pricePerHour;
    private Boolean isPeakHour;
}
```

#### 3. DiscountConfig Entity
```java
@Entity
@Table(name = "discount_configs")
public class DiscountConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Integer minMonths;
    private BigDecimal discountPercent;
    
    @Enumerated(EnumType.STRING)
    private Status status;  // ACTIVE, INACTIVE
}
```

#### 4. RecurringBooking Entity
```java
@Entity
@Table(name = "recurring_bookings")
public class RecurringBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;
    
    @ManyToOne
    @JoinColumn(name = "court_id")
    private Court court;
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    // "MONDAY,WEDNESDAY,FRIDAY"
    private String daysOfWeek;
    
    private LocalTime startTime;
    private LocalTime endTime;
    
    private BigDecimal discountRate;
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    private RecurringBookingStatus status;  // ACTIVE, CANCELLED, COMPLETED
    
    private LocalDateTime createdAt;
}

public enum RecurringBookingStatus {
    ACTIVE, CANCELLED, COMPLETED
}
```

### DTOs - Request

#### CourtSearchRequest
```java
@Data
public class CourtSearchRequest {
    private String date;  // yyyy-MM-dd
    private CourtType courtType;
    private CourtStatus status;
    private Integer page = 0;
    private Integer size = 10;
}
```

#### CheckAvailabilityRequest
```java
@Data
@Validated
public class CheckAvailabilityRequest {
    @NotNull
    private String date;  // yyyy-MM-dd
    
    @NotNull
    @Pattern(regexp = "^([0-1][0-9]|2[0-3]):[0-5][0-9]$")
    private String startTime;  // HH:mm
    
    @NotNull
    @Pattern(regexp = "^([0-1][0-9]|2[0-3]):[0-5][0-9]$")
    private String endTime;
    
    private List<DayOfWeek> daysOfWeek;
    private String startDate;
    private String endDate;
}
```

#### CreateRecurringBookingRequest
```java
@Data
@Validated
public class CreateRecurringBookingRequest {
    @NotNull
    private Long courtId;
    
    @NotNull
    private String startDate;  // yyyy-MM-dd
    
    @NotNull
    private String endDate;
    
    @NotEmpty
    private List<DayOfWeek> daysOfWeek;
    
    @NotNull
    @Pattern(regexp = "^([0-1][0-9]|2[0-3]):[0-5][0-9]$")
    private String startTime;
    
    @NotNull
    @Pattern(regexp = "^([0-1][0-9]|2[0-3]):[0-5][0-9]$")
    private String endTime;
}
```

### DTOs - Response

#### CourtResponse
```java
@Data
@Builder
public class CourtResponse {
    private Long id;
    private String name;
    private CourtType courtType;
    private CourtStatus status;
    private String description;
    private String imageUrl;
    private List<String> facilities;
    private BigDecimal basePrice;
    private PriceRangeDTO priceRange;
}

@Data
@Builder
class PriceRangeDTO {
    private BigDecimal min;
    private BigDecimal max;
}
```

#### TimeSlotResponse
```java
@Data
@Builder
public class TimeSlotResponse {
    private String startTime;
    private String endTime;
    private TimeSlotStatus status;  // AVAILABLE, BOOKED
    private BigDecimal price;
    private String bookedBy;  // REGULAR, RECURRING
}

enum TimeSlotStatus {
    AVAILABLE, BOOKED
}
```

#### AvailabilityResponse
```java
@Data
@Builder
public class AvailabilityResponse {
    private Long courtId;
    private String courtName;
    private String date;
    private DayOfWeek dayOfWeek;
    private List<TimeSlotResponse> timeSlots;
    private OperatingHoursDTO operatingHours;
}

@Data
@Builder
class OperatingHoursDTO {
    private String open;
    private String close;
}
```

#### RecurringBookingResponse
```java
@Data
@Builder
public class RecurringBookingResponse {
    private Long id;
    private CustomerSummaryDTO customer;
    private CourtSummaryDTO court;
    private ScheduleDTO schedule;
    private PricingDTO pricing;
    private ProgressDTO progress;
    private List<String> upcomingDates;
    private RecurringBookingStatus status;
    private LocalDateTime createdAt;
}

@Data
@Builder
class ScheduleDTO {
    private String startDate;
    private String endDate;
    private List<DayOfWeek> daysOfWeek;
    private String startTime;
    private String endTime;
    private String duration;
}

@Data
@Builder
class PricingDTO {
    private BigDecimal pricePerSession;
    private Integer totalSessions;
    private BigDecimal subtotal;
    private BigDecimal discountRate;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
}

@Data
@Builder
class ProgressDTO {
    private Integer totalSessions;
    private Integer completedSessions;
    private Integer upcomingSessions;
    private Integer cancelledSessions;
}
```

---

## 🔧 SERVICES CẦN TRIỂN KHAI

### 1. CourtService
```java
@Service
public class CourtService {
    // Lấy danh sách sân với filter
    List<CourtResponse> getCourts(CourtSearchRequest request);
    
    // Lấy chi tiết sân
    CourtResponse getCourtById(Long courtId);
    
    // CRUD cho STAFF (tạo, sửa, xóa sân)
    CourtResponse createCourt(CreateCourtRequest request);
    CourtResponse updateCourt(Long id, UpdateCourtRequest request);
    void deleteCourt(Long id);
}
```

### 2. CourtAvailabilityService
```java
@Service
public class CourtAvailabilityService {
    // Lấy time slots của sân trong ngày
    AvailabilityResponse getCourtAvailability(Long courtId, LocalDate date);
    
    // Check availability cho recurring booking
    CheckAvailabilityResponse checkAvailability(
        Long courtId, 
        CheckAvailabilityRequest request
    );
    
    // Helper: Check conflict
    boolean hasConflict(Long courtId, LocalDate date, 
                       LocalTime start, LocalTime end);
    
    // Helper: Generate time slots
    List<TimeSlotResponse> generateTimeSlots(LocalDate date);
}
```

### 3. RecurringBookingService
```java
@Service
public class RecurringBookingService {
    // Tạo recurring booking
    RecurringBookingResponse createRecurringBooking(
        CreateRecurringBookingRequest request,
        User currentUser
    );
    
    // Lấy danh sách
    Page<RecurringBookingResponse> getRecurringBookings(
        Long customerId,
        RecurringBookingStatus status,
        Pageable pageable
    );
    
    // Chi tiết
    RecurringBookingResponse getRecurringBookingById(Long id);
    
    // Hủy
    CancelRecurringBookingResponse cancelRecurringBooking(
        Long id, 
        String reason
    );
    
    // Helper: Calculate price
    PricingDTO calculatePricing(
        Court court,
        LocalTime startTime,
        LocalTime endTime,
        LocalDate startDate,
        LocalDate endDate,
        List<DayOfWeek> daysOfWeek
    );
    
    // Helper: Generate individual bookings
    void generateIndividualBookings(RecurringBooking recurringBooking);
}
```

### 4. PriceConfigService
```java
@Service
public class PriceConfigService {
    // Lấy bảng giá
    List<PriceConfigResponse> getPriceConfigs(CourtType courtType);
    
    // Tính giá cho time range
    BigDecimal calculatePrice(
        CourtType courtType,
        LocalTime startTime,
        LocalTime endTime
    );
    
    // CRUD cho MANAGER
    PriceConfigResponse createPriceConfig(CreatePriceConfigRequest request);
    PriceConfigResponse updatePriceConfig(Long id, UpdatePriceConfigRequest request);
    void deletePriceConfig(Long id);
}
```

### 5. DiscountConfigService
```java
@Service
public class DiscountConfigService {
    // Lấy danh sách discount
    List<DiscountConfigResponse> getActiveDiscounts();
    
    // Tính discount dựa trên số tháng
    BigDecimal getDiscountRate(int months);
    
    // CRUD cho MANAGER
    DiscountConfigResponse createDiscount(CreateDiscountRequest request);
    DiscountConfigResponse updateDiscount(Long id, UpdateDiscountRequest request);
    void deleteDiscount(Long id);
}
```

---

## 📝 MIGRATION SCRIPTS

### V4__Add_Court_Details.sql
```sql
-- Thêm mô tả và tiện ích cho sân
ALTER TABLE courts
    ADD COLUMN description TEXT,
    ADD COLUMN facilities TEXT;  -- JSON array

-- Seed data mẫu
UPDATE courts SET 
    description = 'Sân tiêu chuẩn trong nhà với đầy đủ tiện nghi',
    facilities = '["Thiết bị PVC", "Điều hòa"]'
WHERE id = 1;
```

### V5__Seed_Price_Configs.sql
```sql
-- Insert giá cho STANDARD court
INSERT INTO price_configs (court_type, start_time, end_time, price_per_hour, is_peak_hour)
VALUES 
    ('STANDARD', '06:00', '12:00', 100000, false),
    ('STANDARD', '12:00', '17:00', 120000, false),
    ('STANDARD', '17:00', '22:00', 150000, true);

-- Insert giá cho VIP court
INSERT INTO price_configs (court_type, start_time, end_time, price_per_hour, is_peak_hour)
VALUES 
    ('VIP', '06:00', '12:00', 200000, false),
    ('VIP', '12:00', '22:00', 250000, true);
```

### V6__Seed_Discount_Configs.sql
```sql
INSERT INTO discount_configs (min_months, discount_percent, status)
VALUES 
    (1, 5.00, 'ACTIVE'),
    (3, 10.00, 'ACTIVE'),
    (6, 15.00, 'ACTIVE'),
    (12, 20.00, 'ACTIVE');
```

---

## 🎯 WORKFLOW TỔNG QUAN

### User Flow - Đặt Sân Cố Định

```
1. User truy cập màn hình "Khám phá & Đặt sân"
   ↓
2. Chọn ngày và loại sân → Click "Tìm sân trống"
   API: GET /api/courts?date=2026-09-20&courtType=STANDARD
   ↓
3. Xem danh sách sân available
   ↓
4. Click "Xem lịch" trên một sân cụ thể
   API: GET /api/courts/{courtId}/availability?date=2026-09-20
   ↓
5. Chọn khung giờ mong muốn (18:30-20:00)
   - Frontend highlight các slot được chọn
   - Tính tổng tiền realtime
   ↓
6. Click "Đặt sân ngay" → Navigate to form đặt cố định
   ↓
7. Điền form:
   - Ngày bắt đầu / kết thúc
   - Chọn các ngày trong tuần (T2, T4, T6)
   - Xem preview lịch
   - Xem giá + discount
   ↓
8. Click "Kiểm tra khả dụng"
   API: POST /api/courts/{courtId}/check-availability
   ↓
9. Nếu available:
   - Hiển thị tổng tiền, discount
   - Button "Xác nhận đặt sân"
   ↓
10. Click "Xác nhận đặt sân"
    API: POST /api/recurring-bookings
    ↓
11. Redirect to Payment (hoặc thành công nếu payment sau)
```

---

## ✅ CHECKLIST TRIỂN KHAI

### Phase 1: Database & Entities (2-3 ngày)
- [ ] Tạo migration V4, V5, V6
- [ ] Cập nhật Court entity (description, facilities)
- [ ] Tạo PriceConfig entity
- [ ] Tạo DiscountConfig entity
- [ ] Cập nhật RecurringBooking entity (sửa role thành enum)
- [ ] Tạo enums: CourtType, CourtStatus, RecurringBookingStatus
- [ ] Test migrations

### Phase 2: Repositories (1 ngày)
- [ ] CourtRepository
- [ ] PriceConfigRepository
- [ ] DiscountConfigRepository
- [ ] RecurringBookingRepository
- [ ] Custom queries nếu cần

### Phase 3: DTOs (1-2 ngày)
- [ ] Request DTOs (10+ classes)
- [ ] Response DTOs (10+ classes)
- [ ] Validation annotations
- [ ] Mappers/Converters

### Phase 4: Services - Courts (2-3 ngày)
- [ ] CourtService (CRUD)
- [ ] PriceConfigService
- [ ] DiscountConfigService
- [ ] Test services

### Phase 5: Services - Availability (3-4 ngày)
- [ ] CourtAvailabilityService
- [ ] Logic generate time slots
- [ ] Logic check conflicts
- [ ] Calculate pricing logic
- [ ] Test edge cases

### Phase 6: Services - Recurring Booking (4-5 ngày)
- [ ] RecurringBookingService
- [ ] Create recurring booking logic
- [ ] Availability checking logic
- [ ] Pricing calculation với discount
- [ ] Generate individual bookings (optional)
- [ ] Cancel logic với refund
- [ ] Test toàn bộ flow

### Phase 7: Controllers (2-3 ngày)
- [ ] CourtsController (5 endpoints)
- [ ] RecurringBookingsController (4 endpoints)
- [ ] PriceConfigsController (1 endpoint)
- [ ] DiscountConfigsController (1 endpoint)
- [ ] Exception handling
- [ ] API documentation (comments)

### Phase 8: Security & Authorization (1-2 ngày)
- [ ] Cập nhật SecurityConfig
- [ ] Permission checks trong services
- [ ] Test phân quyền

### Phase 9: Testing (3-4 ngày)
- [ ] Unit tests cho services
- [ ] Integration tests cho APIs
- [ ] Test với Postman
- [ ] Test edge cases
- [ ] Load testing (optional)

### Phase 10: Documentation & Deployment (1-2 ngày)
- [ ] Update Api.md
- [ ] Tạo Postman collection
- [ ] README cho recurring booking feature
- [ ] Deploy to dev/staging
- [ ] Bug fixes

---

## 📊 ESTIMATE TIMELINE

**Tổng thời gian ước tính**: 20-30 ngày làm việc

- **Nếu 1 developer full-time**: ~4-6 tuần
- **Nếu team 2 developers**: ~2-3 tuần
- **Nếu team 3+ developers**: ~1.5-2 tuần

**Breakdown**:
- Backend core: 15-20 ngày
- Testing: 3-4 ngày
- Documentation: 1-2 ngày
- Buffer: 1-4 ngày

---

## 🚨 RỦI RO & MITIGATION

### Rủi Ro 1: Time Slot Conflicts
**Vấn đề**: Race condition khi 2 users đặt cùng lúc

**Giải pháp**:
- Sử dụng database locking
- Optimistic locking với `@Version` trong Entity
- Transaction isolation level

### Rủi Ro 2: Performance với Large Data
**Vấn đề**: Query chậm khi có nhiều bookings

**Giải pháp**:
- Indexes đã có trong V1__Create_DB.sql
- Thêm caching cho availability (Redis)
- Pagination

### Rủi Ro 3: Price Calculation Complexity
**Vấn đề**: Logic tính giá phức tạp với nhiều time ranges

**Giải pháp**:
- Unit tests kỹ lưỡng
- Helper methods rõ ràng
- Documentation đầy đủ

### Rủi Ro 4: Recurring Booking Cancellation
**Vấn đề**: Refund policy phức tạp

**Giải pháp**:
- Define rõ business rules trước
- Flexible configuration
- Log đầy đủ transactions

---

## 🎓 RECOMMENDATIONS

### 1. Start Small
- Implement màn hình 1 trước (Courts listing)
- Test kỹ trước khi sang màn hình 2

### 2. API-First Approach
- Design APIs trước
- Mock data để frontend test song song
- Contract testing

### 3. Automated Testing
- Unit tests cho business logic
- Integration tests cho APIs
- Test coverage > 80%

### 4. Code Review
- Review kỹ pricing logic
- Review security & permissions
- Review performance

### 5. Documentation
- Comment code đầy đủ
- API docs rõ ràng
- Update README

---

**Tạo bởi**: Kiro AI  
**Ngày**: 2026-09-26  
**Phiên bản**: 1.0  
**Status**: Planning - Ready for Implementation

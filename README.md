# FashionStore - Ứng dụng E-commerce Desktop

Ứng dụng bán hàng thời trang desktop được xây dựng bằng Java và JavaFX, kết nối với MySQL qua XAMPP.

## Yêu cầu hệ thống

- Java 17 hoặc cao hơn
- Maven 3.6+
- XAMPP (MySQL)
- IDE hỗ trợ Java (IntelliJ IDEA, Eclipse, VS Code)

## Cài đặt

### 1. Cài đặt Database

1. Khởi động XAMPP và bật MySQL
2. Mở phpMyAdmin hoặc MySQL command line
3. Chạy file `database/schema.sql` để tạo database và dữ liệu mẫu:

```sql
-- Mở MySQL trong XAMPP và chạy:
source database/schema.sql
```

Hoặc import file `database/schema.sql` qua phpMyAdmin.

### 2. Cấu hình Database Connection

Nếu cần thay đổi thông tin kết nối, chỉnh sửa file `src/main/java/com/fashionstore/database/DatabaseConnection.java`:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/fashionstore";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = ""; // Mật khẩu MySQL của bạn
```

### 3. Build và Chạy ứng dụng

#### Trên NetBeans (Khuyến nghị):

1. Mở NetBeans IDE
2. **File → Open Project** → Chọn thư mục `Shop`
3. Click chuột phải project → **Clean and Build** (`Shift + F11`)
4. Click nút **Run** (▶) hoặc nhấn `F6`

Xem chi tiết trong file `HUONG_DAN_NETBEANS.md`

#### Sử dụng Maven (Command Line):

```bash
# Build project
mvn clean compile

# Chạy ứng dụng
mvn javafx:run
```

#### Hoặc chạy trực tiếp:

```bash
# Compile
mvn compile

# Run
java --module-path "path/to/javafx/lib" --add-modules javafx.controls,javafx.fxml -cp target/classes com.fashionstore.Main
```

## Cấu trúc dự án

```
Shop/
├── database/
│   └── schema.sql              # Database schema và dữ liệu mẫu
├── src/
│   └── main/
│       ├── java/
│       │   └── com/fashionstore/
│       │       ├── Main.java                    # Entry point
│       │       ├── controllers/                # UI Controllers
│       │       │   ├── HomeController.java
│       │       │   ├── ProductListController.java
│       │       │   ├── ProductDetailController.java
│       │       │   ├── CartController.java
│       │       │   └── AccountController.java
│       │       ├── models/                     # Data Models
│       │       │   └── Product.java
│       │       ├── dao/                        # Data Access Objects
│       │       │   └── ProductDAO.java
│       │       └── database/                   # Database Connection
│       │           └── DatabaseConnection.java
│       └── resources/
│           └── styles.css                      # CSS Styling
├── pom.xml                                     # Maven dependencies
└── README.md
```

## Tính năng

- ✅ Trang chủ với banner, categories, sản phẩm nổi bật
- ✅ Danh sách sản phẩm với filter và sort
- ✅ Chi tiết sản phẩm với color/size selection
- ✅ Giỏ hàng với tính năng thêm/xóa sản phẩm
- ✅ Trang tài khoản với thông tin user, điểm tích lũy, đơn hàng
- ✅ Kết nối database MySQL qua XAMPP
- ✅ Giao diện đẹp mắt, responsive cho desktop

## Các màn hình

1. **Homepage** - Trang chủ với banner, categories, featured products
2. **Product List** - Danh sách sản phẩm với filter
3. **Product Detail** - Chi tiết sản phẩm
4. **Shopping Cart** - Giỏ hàng
5. **Account** - Tài khoản người dùng

## Lưu ý

- Đảm bảo MySQL trong XAMPP đã được khởi động trước khi chạy ứng dụng
- Database mặc định: `fashionstore`, user: `root`, password: `""` (rỗng)
- Nếu gặp lỗi kết nối database, kiểm tra lại thông tin trong `DatabaseConnection.java`

## Phát triển tiếp

- Thêm chức năng đăng nhập/đăng ký
- Tích hợp thanh toán
- Quản lý đơn hàng
- Upload hình ảnh sản phẩm
- Tìm kiếm sản phẩm


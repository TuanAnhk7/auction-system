# 🔨 Hệ thống Đấu giá Trực tuyến (Online Auction System)

Một hệ thống đấu giá trực tuyến theo thời gian thực được xây dựng bằng kiến trúc **Client-Server**, giao diện **JavaFX** và áp dụng các **Mẫu thiết kế (Design Patterns)** chuẩn mực.

Dự án này là Bài tập lớn môn Lập trình nâng cao, hỗ trợ nhiều người chơi tham gia đấu giá cùng lúc (Multi-threading) thông qua kết nối mạng Socket.

---

## 🌟 Tính năng nổi bật

### Phân quyền người dùng
* **Admin:** Quản lý toàn bộ hệ thống và người dùng.
* **Seller:** Thêm, sửa, xóa vật phẩm đấu giá; quản lý các phiên đấu giá của mình.
* **Bidder:** Tham gia các phiên đấu giá đang mở, theo dõi lịch sử trả giá.

### Tính năng cốt lõi
* **Live Bidding (Realtime):** Cập nhật giá liên tục trên màn hình của tất cả người chơi ngay khi có người đặt giá mới nhờ Socket và Observer Pattern.
* **Xử lý Đa luồng (Concurrency):** Server phục vụ hàng trăm Client kết nối đồng thời nhờ cơ chế Multi-threading, đảm bảo không bị nghẽn mạng hay khóa giao diện.
* **Kiểm soát tính hợp lệ:** Xử lý ngoại lệ chặt chẽ, từ chối các mức giá thấp hơn giá hiện tại hoặc các thao tác khi phiên đấu giá đã kết thúc.

---

## 🛠 Công nghệ sử dụng
* **Ngôn ngữ:** Java (JDK 17+)
* **Giao diện:** JavaFX & SceneBuilder (Mô hình MVC)
* **Mạng & Dữ liệu:** Java Socket, Serialization (I/O Streams)
* **Quản lý dự án:** Maven
* **Kiểm thử & CI/CD:** JUnit 5, GitHub Actions, Checkstyle

---

## 🏛 Kiến trúc & Mẫu thiết kế (Design Patterns)
Dự án tuân thủ nghiêm ngặt các nguyên lý OOP (SOLID) và áp dụng các mẫu thiết kế:
1. **MVC (Model-View-Controller):** Tách biệt hoàn toàn giao diện (FXML) và logic xử lý (Controllers).
2. **Singleton:** Đảm bảo chỉ có một thể hiện duy nhất của `AuctionManager` quản lý mọi phiên đấu giá.
3. **Observer (Lắng nghe):** Thông báo đồng loạt (Broadcast) tới các Client khi có sự thay đổi về giá.
4. **Factory Method:** Khởi tạo các loại vật phẩm đấu giá khác nhau (Electronics, Art, Vehicle) một cách linh hoạt.

---
## 📂 Cấu trúc Module chính
Dự án áp dụng mô hình Maven Multi-module, được chia thành 3 phần tách biệt:
* **`common`**: Chứa các đối tượng dữ liệu dùng chung (User, Item, Request/Response Packets...) phục vụ cho quá trình đóng gói và truyền tải qua mạng (Serialization).
* **`server`**: Module máy chủ không giao diện (Console-based). Đảm nhiệm việc mở Socket port 8080, quản lý danh sách người chơi (Multi-threading), xử lý logic đấu giá và phát sóng (Broadcast) giá mới đến toàn mạng.
* **`client`**: Module khách hàng (JavaFX UI). Giao tiếp với Server để thực hiện đăng nhập, hiển thị danh sách vật phẩm và gửi lệnh đặt giá (Live Bidding).

---

## 📦 Vị trí các file đóng gói (.jar)
Sau khi sử dụng Maven Lifecycle để biên dịch toàn bộ dự án (chạy lệnh `install` ở thư mục root), các file thực thi `.jar` sẽ được tự động sinh ra và lưu trữ tại các vị trí sau:
* **Gói thư viện dùng chung:** `common/target/common-1.0-SNAPSHOT.jar`
* **File chạy Server:** `server/target/server-1.0-SNAPSHOT.jar`
* **File chạy Client:** `client/target/client-1.0-SNAPSHOT.jar`
  *(Lưu ý: Tên phiên bản 1.0-SNAPSHOT có thể thay đổi tùy theo thiết lập trong file pom.xml)*

## 🚀 Hướng dẫn Cài đặt & Chạy dự án

### Yêu cầu hệ thống
* Đã cài đặt **Java JDK 17** trở lên.
* Đã cài đặt **Maven**.

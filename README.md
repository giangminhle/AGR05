# AGR-05: Hệ thống quản lý cảnh báo sâu bệnh dựa trên báo cáo hiện trường có xác minh

Đồ án cuối kỳ môn **Phương pháp phát triển phần mềm hướng đối tượng**.

- Sinh viên: Lê Minh Giang, MSSV 1250080043, lớp 12_DH_CNPM1
- Giảng viên: ThS. Phạm Trọng Huynh

Nông dân gửi báo cáo sâu bệnh kèm ảnh và tọa độ, chuyên gia phụ trách khu vực xác minh, hệ thống thống kê và phát cảnh báo khi số ca vượt ngưỡng. Mô-đun AI dự báo nguy cơ bùng phát trong 7 ngày tới dựa trên các báo cáo đã xác minh. Phân tích, thiết kế và kết quả kiểm thử nằm trong báo cáo Word ở `bao-cao/word/`.

## Chức năng chính

| Vai trò | Làm được gì |
|---|---|
| Nông dân | Đăng ký, gửi báo cáo (1–5 ảnh, lấy vị trí GPS), theo dõi kết quả, sửa và gửi lại một lần khi bị bác bỏ |
| Chuyên gia | Xem báo cáo chờ xác minh của khu vực mình phụ trách, duyệt hoặc bác bỏ, xem thống kê và cảnh báo |
| Quản trị viên | Quản lý tài khoản, khu vực, loại sâu bệnh, tham số hệ thống; huấn luyện và kích hoạt mô hình AI |

## Công nghệ

- Backend: Java 21, Spring Boot 3.5 (REST API), Spring JDBC, Spring Security
- Frontend: React (Vite)
- Cơ sở dữ liệu: SQL Server
- AI: thư viện Smile (GradientTreeBoost)
- Kiểm thử: JUnit 5, ArchUnit

## Cấu trúc thư mục

| Thư mục | Nội dung |
|---|---|
| `backend/` | Mã nguồn Spring Boot, chia gói `api` (Controller), `bll` (Service), `dal` (truy cập dữ liệu), `entity`, `config`; test nằm trong `src/test` |
| `frontend/` | Giao diện React: `pages/` các trang, `components/` thành phần dùng chung, `api/` gọi backend |
| `db/` | `AGR05.sql` tạo CSDL 15 bảng, `seed_demo.sql` dữ liệu danh mục mẫu, `lam_moi_du_lieu_mau.sql` làm mới dữ liệu demo |
| `docs/` | Đặc tả yêu cầu, mô hình miền, luồng xử lý, từ điển dữ liệu, mô-đun AI, giao diện, test case, kết quả kiểm thử |
| `docs/so-do/` | Các sơ đồ UML trong báo cáo (tệp `.drawio` gốc và ảnh `.png`) |
| `bao-cao/` | Báo cáo Word, slide thuyết trình, sơ đồ draw.io |
| `cong-cu/` | Script xuất bảng kết quả kiểm thử và thử API |

## Cài đặt và chạy

### 1. Phần mềm cần có

- JDK 21 trở lên, Maven 3.9
- Node.js 20 trở lên (kèm npm)
- SQL Server (Express hoặc Developer) và SSMS hoặc `sqlcmd`

### 2. Tạo cơ sở dữ liệu

```
sqlcmd -S . -E -I -f 65001 -i db\AGR05.sql
sqlcmd -S . -E -I -f 65001 -i db\seed_demo.sql
```

Tên server có thể khác `.` (ví dụ `.\SQLEXPRESS`), xem trong SSMS. Phải có `-I` (bật QUOTED_IDENTIFIER, cần cho index có lọc) và `-f 65001` (đọc file UTF-8 để không lỗi tiếng Việt). Chạy bằng SSMS cũng được.

### 3. Cấu hình kết nối

Chép `backend/src/main/resources/application-example.yml` thành `application.yml` rồi sửa `url`, `username`, `password`. SQL Server cần bật Mixed Mode và TCP cổng 1433. File `application.yml` có mật khẩu nên đã được loại khỏi Git, đừng commit file này.

### 4. Chạy

```
cd backend
mvn spring-boot:run
```

Lần chạy đầu, log in ra tài khoản `admin` kèm mật khẩu ngẫu nhiên (chỉ in một lần, nhớ chép lại).

```
cd frontend
npm install
npm run dev
```

Mở http://localhost:5173.

Nếu cổng 8080 đang bị chiếm (máy em bị VMware chiếm), đặt `server.port: 8081` trong `application.yml` và tạo `frontend/.env.local` có dòng `AGR05_API=http://localhost:8081`.

### 5. Dữ liệu mẫu để demo

```
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

Chạy sau `db/seed_demo.sql`. Lệnh này tạo 7 tài khoản (`qt.minhkhoa`, `cg.thanhtam`, `cg.ngocanh`, `nd.vanbay`, `nd.thimai`, `nd.vantu`, `nd.vanut`), khoảng 880 báo cáo lịch sử để huấn luyện AI và mô hình AI đầu tiên. Mật khẩu các tài khoản này lấy từ `agr05.mat-khau-mau` trong `application.yml` hoặc biến môi trường `AGR05_MAT_KHAU_MAU`; không đặt thì hệ thống sinh ngẫu nhiên và in ra log. Muốn làm mới dữ liệu demo thì chạy `db/lam_moi_du_lieu_mau.sql`.

Số liệu lịch sử này là dữ liệu mô phỏng, chỉ dùng để chạy thử, không phải dữ liệu thật của địa phương.

### 6. Đóng gói chạy một lệnh

Chạy `dong-goi.bat`: build giao diện, chạy test và đóng gói cả API lẫn giao diện vào `phat-hanh\agr05.jar`, kèm `application.yml` và `chay.bat`. Sau đó chạy `phat-hanh\chay.bat` và mở http://localhost:8081 (cổng theo `phat-hanh\application.yml`).

## Kiểm thử

```
cd backend
mvn verify
```

Có 72 test, chạy không cần CSDL (dùng DAL giả). Lần chạy gần nhất đạt 72/72. Kết quả TC01–TC16 ở `docs/ket-qua-kiem-thu.md`, xuất lại bằng `python cong-cu/xuat_ket_qua_kiem_thu.py` sau khi chạy `mvn verify`.

Test tích hợp với SQL Server thật: đặt biến môi trường `AGR05_TEST_DB` là chuỗi JDBC đầy đủ rồi chạy `mvn verify -Pintegration`.

## Ghi chú

- Những chỗ cài đặt khác hoặc bổ sung so với thiết kế ban đầu được ghi trong `docs/thay-doi-so-voi-bao-cao.md`.
- Mô-đun AI mới chạy trên dữ liệu mô phỏng nên các chỉ số (Accuracy, F1, AUC) chỉ để kiểm tra quy trình huấn luyện, chưa phản ánh độ chính xác thực tế.

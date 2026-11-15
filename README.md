# AGR-05: bộ khởi đầu để code bằng Claude Code (Java Spring Boot + React)

## 1. Cài trên máy

- JDK 21 trở lên (máy hiện có JDK 24) và Maven 3.9.
- Node.js 20 trở lên (kèm npm) để chạy giao diện React.
- SQL Server Express hoặc Developer, kèm SQL Server Management Studio (SSMS) hoặc `sqlcmd`.
- Git.
- Claude Code: cài theo hướng dẫn chính thức tại https://docs.claude.com (mục Claude Code), rồi đăng nhập.

## 2. Chuẩn bị

```
cd đường\dẫn\tới\AGR05
git init
git add . && git commit -m "Bo khoi dau tu bao cao"
sqlcmd -S . -E -I -f 65001 -i db\AGR05.sql
sqlcmd -S . -E -I -f 65001 -i db\seed_demo.sql
```

Tên server có thể khác `.` (ví dụ `.\SQLEXPRESS`); xem tên server trong SSMS. Hai tùy chọn `-I` (bật QUOTED_IDENTIFIER, cần cho index lọc) và `-f 65001` (đọc file UTF-8, giữ tiếng Việt) là bắt buộc.

## 2b. Chạy hệ thống (đã code xong)

1. Chép `backend/src/main/resources/application-example.yml` thành `application.yml`, sửa `url`, `username`, `password` (cần SQL Server bật Mixed Mode và TCP 1433). File này không commit.
2. Backend: `cd backend` rồi `mvn spring-boot:run`. Lần đầu chạy, log in ra tài khoản `admin` và mật khẩu ngẫu nhiên (chỉ in một lần).
3. Frontend: `cd frontend`, `npm install`, `npm run dev`, mở http://localhost:5173.
4. Nếu cổng 8080 bị chiếm (ví dụ VMware dùng): đặt `server.port: 8081` trong `application.yml` và tạo `frontend/.env.local` chứa `AGR05_API=http://localhost:8081`.
5. Dữ liệu mẫu: `mvn spring-boot:run -Dspring-boot.run.profiles=demo` (chạy sau `db/seed_demo.sql`). Tạo 7 tài khoản (`qt.minhkhoa`, `cg.thanhtam`, `cg.ngocanh`, `nd.vanbay`, `nd.thimai`, `nd.vantu`, `nd.vanut`, mật khẩu `Agr05@2026`), khoảng 880 báo cáo lịch sử có ảnh thật để huấn luyện AI, tình huống demo và mô hình AI đầu tiên. Số liệu lịch sử là mô phỏng, không dùng làm kết quả cho báo cáo. Làm mới: `db/lam_moi_du_lieu_mau.sql`.
6. Test: `cd backend && mvn verify` (72 test, không cần CSDL). Test tích hợp: đặt biến `AGR05_TEST_DB` là chuỗi JDBC đầy đủ rồi chạy `mvn verify -Pintegration`.

## 2c. Bản đóng gói chạy một lệnh (dùng khi demo)

1. Chạy `dong-goi.bat`: build giao diện, chạy test, đóng gói API + giao diện vào `phat-hanh\agr05.jar`, kèm `application.yml` và `chay.bat`.
2. Chạy `phat-hanh\chay.bat`, mở http://localhost:8081 (cổng lấy từ `phat-hanh\application.yml`).
3. Kịch bản demo từng bước, tài khoản demo, cách làm mới dữ liệu demo: `docs/kich-ban-demo.md`.
4. Bảng kết quả TC01–TC16 cho mục 7.2: `docs/ket-qua-kiem-thu.md` (xuất lại bằng `python cong-cu/xuat_ket_qua_kiem_thu.py` sau `mvn verify`).
5. Mọi chỗ code khác hoặc thêm so với báo cáo Word: `docs/thay-doi-so-voi-bao-cao.md`.

## 3. Làm việc với Claude Code

1. Mở terminal trong thư mục `AGR05`, gõ `claude`.
2. Mở `PROMPTS.md`, dán lần lượt câu lệnh Bước 0, Bước 1, ...
3. Sau mỗi bước: chạy `mvn verify` (trong `backend/`), `npm run build` (trong `frontend/` nếu bước có giao diện), thử chương trình, rồi commit.
4. Claude Code báo code lệch với `docs/` ở chỗ nào thì ghi lại. Chỗ nào bạn đồng ý đổi thì sửa luôn báo cáo Word cho khớp.

## 4. Nội dung thư mục

| Đường dẫn | Là gì |
|---|---|
| `CLAUDE.md` | Luật của dự án. Claude Code tự đọc mỗi lần chạy |
| `PROMPTS.md` | Câu lệnh cho 12 bước |
| `docs/` | Trích từ báo cáo: yêu cầu, use case, lớp, luồng, CSDL, AI, giao diện, test case |
| `db/AGR05.sql` | Script tạo CSDL 15 bảng (đã sửa các lỗi ghi ở mục 5) |
| `db/seed_demo.sql` | Khu vực và loại sâu bệnh mẫu để chạy thử |
| `backend/` | (tạo ở Bước 1) Spring Boot REST API |
| `frontend/` | (tạo ở Bước 1) giao diện React |

## 5. Các chỗ đã sửa so với script trong báo cáo v4

Những chỗ này cũng cần sửa trong Chương IV của báo cáo Word cho khớp:

1. Thêm dòng `/*` mở đầu comment (bản cũ thiếu nên chạy sẽ lỗi).
2. Cột `TriChung` → `TrieuChung` (khớp ERD và biểu đồ lớp).
3. Bảng MoHinhAI, DuBaoNguyCo tạo trước CanhBao; CanhBao có sẵn cột `MaDuBao` thay vì ALTER ở cuối.
4. Thêm CHECK `NGUONG_NGUY_CO` từ 1 đến 100; CHECK chỉ số mô hình trong 0..1.
5. Thêm index `IX_CanhBao_KhuVuc_Loai_NgayTao` (kiểm tra cảnh báo trùng) và `IX_DuBao_MoHinh`.
6. `docs/data-dictionary.md` đã sửa: quy tắc 2 (có liên kết 1 – 0..1), quy tắc 9 và bảng tham số có NGUONG_NGUY_CO, BaoCao có 4 bảng con, RB08 dùng `DanhMucService.phanCong`, MoHinhAI tách 3 cột Accuracy, F1, AUC.

## 6. Đổi công nghệ từ C# WinForms sang Java Spring Boot + React

Bản gốc của bộ khởi đầu dùng C# .NET 8 WinForms, ADO.NET và ML.NET. Đã đổi sang Spring Boot (REST API) + React, Spring JDBC (JdbcTemplate), SQL Server và thư viện Smile. Nghiệp vụ, use case, CSDL và tên lớp giữ nguyên; tên hàm viết theo quy ước Java (camelCase). Các phần báo cáo Word phải sửa cho khớp:

| Phần báo cáo | Sửa gì |
|---|---|
| Mục 5.1.1 Framework | Bảng công nghệ: Java 21, Spring Boot, REST, React, JdbcTemplate, Spring Security, Smile |
| Mục 5.1.2 và Biểu đồ 25 (lớp thiết kế) | Tầng trình bày = trang React + REST Controller; thêm các lớp Controller, HanXuLyScheduler; tên hàm camelCase; DAL không nhận tham số transaction (Service mở bằng TransactionTemplate) |
| Mục 5.2.1–5.2.4 và Biểu đồ 26–29 | Đổi tên Form sang trang React và Controller, thêm bước gọi API |
| ARCH01, ARCH04, ARCH05, FR-16, FR-17 (Chương I) | Theo `docs/spec.md` mới: Timer trên FrmMain → hẹn giờ 60 giây trên giao diện + tác vụ nền phía server; ML.NET → Smile |
| Đặc tả UC01, UC03, UC05, UC06, UC12, UC17, UC19 | Đổi tên Form, PictureBox, Timer theo `docs/spec.md` mới |
| Mục 3.1.4 (lớp tham gia use case) | Boundary = trang React + Controller |
| Biểu đồ 11–20 (10 sơ đồ tuần tự) | Lifeline Boundary: `FrmX` → `XPage` + `XController`; tên thông điệp camelCase |
| Chương VI (12 màn hình, Hình 1–10) | Thay bằng trang web theo `docs/ui.md` mới (vẽ lại khung giao diện) |
| Mục 5.2.5 (mô-đun AI) và bảng MoHinhAI | FastTree / ML.NET → GradientTreeBoost / Smile; file mô hình không còn là `.zip` |
| Mục 7.2 (test case) | xUnit → JUnit 5 |

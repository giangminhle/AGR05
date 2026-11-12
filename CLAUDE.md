# AGR-05: Hệ thống quản lý cảnh báo sâu bệnh dựa trên báo cáo hiện trường có xác minh

Đồ án môn Phương pháp phát triển phần mềm hướng đối tượng. Code phải khớp với bản phân tích, thiết kế trong `docs/`, vì giảng viên sẽ đối chiếu code với báo cáo khi vấn đáp.

## Nguồn sự thật

Đọc file liên quan TRƯỚC khi viết code. Khi `docs/` và ý tưởng của bạn khác nhau thì làm theo `docs/`. Nếu thấy `docs/` sai hoặc thiếu, dừng lại và hỏi; không tự đổi thiết kế.

| File | Nội dung |
|---|---|
| `docs/spec.md` | BR01–BR08, SEC01–03, ARCH01–05, FR-01–19, NFR, 19 use case kèm luồng chính, thay thế, ngoại lệ |
| `docs/domain-model.md` | 12 lớp thực thể, quan hệ, thuộc tính, phương thức, bảng chuyển trạng thái |
| `docs/flows.md` | Mô tả 10 sơ đồ tuần tự và 3 sơ đồ hoạt động: thứ tự gọi Trang React → Controller → Service → DAL |
| `docs/architecture.md` | Framework, các tầng, trách nhiệm từng Service, danh sách API, danh sách lớp và hàm |
| `docs/data-dictionary.md` | 15 bảng, 87 cột, ràng buộc RB01–RB08 |
| `docs/ai-module.md` | Bài toán, nhãn, đặc trưng, thuật toán, đánh giá, tích hợp mô hình AI |
| `docs/ui.md` | 12 trang web, thành phần và hành vi từng trang |
| `docs/test-cases.md` | TC01–TC16 |
| `db/AGR05.sql` | Script tạo CSDL (15 bảng). Đây là schema chuẩn |

## Công nghệ (không đổi)

- Backend: **Java 21** (biên dịch được bằng JDK 21 trở lên), **Spring Boot 3.5**, build bằng **Maven**.
- Kiểu ứng dụng: **REST API** (JSON) + **frontend React** chạy trên trình duyệt.
- Frontend: **React 18 + Vite**, JavaScript, React Router, Bootstrap 5 (`react-bootstrap`), biểu đồ bằng `recharts`.
- CSDL: **SQL Server**, driver `com.microsoft.sqlserver:mssql-jdbc`.
- Truy cập dữ liệu: **Spring JDBC (`JdbcTemplate` / `NamedParameterJdbcTemplate`)**, viết SQL tay có tham số. KHÔNG dùng JPA, Hibernate, MyBatis hay ORM nào khác.
- Transaction: `TransactionTemplate` của Spring (mở, Commit, Rollback tường minh, giống sơ đồ tuần tự).
- Bảo mật: **Spring Security**, đăng nhập bằng phiên (session cookie), phân quyền theo vai trò.
- Học máy: **Smile** (`com.github.haifengl:smile-core`), thuật toán `GradientTreeBoost` (cây quyết định tăng cường).
- Kiểm thử backend: **JUnit 5** (+ AssertJ), kiểm tra kiến trúc bằng **ArchUnit**.

## Cấu trúc thư mục

```
backend/                         Maven, Spring Boot (artifact agr05-backend)
  src/main/java/vn/agr05/
    entity/      lớp thực thể (POJO) + kiểm tra nghiệp vụ của chính nó, hằng số mã trạng thái
    dal/         mỗi bảng một lớp ...Dal + interface I...Dal (chỉ nơi này có SQL)
    bll/         ...Service, AnhStorage, AIService, AppSession, DbSeeder, HanXuLyScheduler
    api/         ...Controller (REST), DTO vào/ra, xử lý lỗi chung
    config/      cấu hình Spring Security, đọc application.yml
  src/main/resources/application-example.yml
  src/test/java/vn/agr05/        JUnit (fake DAL), test tích hợp, test kiến trúc
frontend/                        React + Vite
  src/pages/     12 trang (mục 6.1 docs/ui.md)
  src/components/  thành phần dùng chung (khung trang, ô nhập có báo lỗi, bảng...)
  src/api/       hàm gọi REST API (fetch), không chứa nghiệp vụ
db/                              script SQL
```

Phụ thuộc chỉ được đi một chiều: `api` → `bll` → `dal` → `entity`. `api` được dùng `entity` để đóng gói DTO. `api` không gọi `dal` và không dùng `JdbcTemplate`. Test ArchUnit kiểm tra luật này. Frontend chỉ gọi REST API, không chứa quy tắc nghiệp vụ (chỉ kiểm tra hình thức để báo lỗi sớm; kiểm tra thật nằm ở backend).

## Luật bắt buộc

1. **Đặt tên đúng thiết kế.** Tên lớp, tên hàm, tên tham số theo `docs/architecture.md` và `docs/flows.md`, viết theo quy ước Java: lớp PascalCase, hàm và biến camelCase, tiếng Việt không dấu (ví dụ `XacMinhService.xacMinh(maBaoCao, maLoai, khuyenNghi, maChuyenGia)`, `BaoCaoDal.updateTrangThai(bc, trangThaiCu)`). URL API theo bảng trong `docs/architecture.md`.
2. **SQL chỉ nằm trong DAL**, luôn dùng tham số `?` hoặc `:ten` (SEC02). Không nối chuỗi SQL với dữ liệu người dùng.
3. **Transaction:** gửi báo cáo, sửa / gửi lại, duyệt, từ chối, đánh dấu đã xử lý, đóng hồ sơ: mỗi thao tác là MỘT transaction (`TransactionTemplate`) gồm cập nhật báo cáo + ghi `LichSuXacMinh` + tạo `ThongBao`. Lỗi thì Rollback; nếu đã lưu file ảnh thì xóa file đó.
4. **Chống hai người cùng xử lý:** đổi trạng thái bằng `UPDATE ... WHERE MaBaoCao = ? AND MaTrangThai = ?` (trạng thái cũ). Số dòng bị ảnh hưởng = 0 → Rollback và báo "Báo cáo đã được xử lý" (TC08).
5. **Mã trạng thái** dùng hằng số trong entity: `CHO_XAC_MINH`, `DA_XAC_MINH`, `BI_BAC_BO`, `DA_XU_LY`. Không viết chuỗi rải rác (cả ở frontend: dùng file hằng số).
6. **Không viết cứng tham số.** Đọc `HAN_XAC_MINH_GIO`, `NGUONG_CANH_BAO`, `SO_NGAY_XET_NGUONG`, `HAN_GUI_LAI_NGAY`, `NGUONG_NGUY_CO` qua `ThamSoService.layGiaTri` (ARCH03).
7. **Kiểm tra nghiệp vụ đặt đúng chỗ:** kiểm tra trên một đối tượng nằm trong entity (`BaoCaoHienTruong.kiemTraHopLe`, `choPhepSua`, `laQuaHan`, `chuyenTrangThai`); kiểm tra cần CSDL (quyền khu vực BR05, ngưỡng BR06) nằm trong Service. Controller không chứa nghiệp vụ.
8. **Quyền (BR05):** chuyên gia chỉ xử lý báo cáo thuộc khu vực trong `PhanCongKhuVuc` và không phải báo cáo do chính mình gửi; nông dân chỉ xem, sửa báo cáo của mình. Kiểm tra ở Service, không chỉ ẩn nút trên giao diện. Người đang đăng nhập lấy từ `AppSession`, không nhận mã người dùng từ request.
9. **Mật khẩu (SEC01):** PBKDF2 (`SecretKeyFactory` thuật toán `PBKDF2WithHmacSHA256`, 100 000 vòng, khóa 256 bit, muối 16 byte ngẫu nhiên `SecureRandom` cho mỗi tài khoản), lưu Base64 vào `MatKhauHash` và `Muoi`. So sánh bằng `MessageDigest.isEqual`.
10. **Ảnh (ARCH02):** nhận file qua `multipart/form-data`, lưu vào thư mục cấu hình `agr05.thu-muc-anh`, CSDL chỉ lưu đường dẫn. Chỉ nhận .jpg/.jpeg/.png (kiểm tra cả phần mở rộng và chữ ký file), tối đa 5 MB, 1–5 ảnh (BR01). Ảnh trả về qua API có kiểm tra quyền, không mở thư mục ảnh ra ngoài.
11. **AI (BR08, ARCH05):** `AIService.duBaoNguyCo` chỉ được gọi SAU khi transaction duyệt đã Commit. Mọi lỗi của mô hình được bắt trong AIService, ghi nhật ký và trả về `null`; thao tác duyệt vẫn thành công và hệ thống chỉ dùng BR06. Không tạo cảnh báo trùng cùng khu vực, cùng loại trong `SO_NGAY_XET_NGUONG` ngày. Chỉ dùng báo cáo `DA_XAC_MINH` và `DA_XU_LY` để huấn luyện. Chia dữ liệu theo thời gian 80/20, không chia ngẫu nhiên. Kích hoạt bản mới chỉ khi F1 ≥ F1 bản đang dùng.
12. **Không xóa báo cáo.** DAL không có hàm xóa báo cáo (RB06).
13. **Cấu hình** (chuỗi kết nối, thư mục ảnh, thư mục mô hình) đọc từ `application.yml` hoặc biến môi trường. Không commit file có mật khẩu thật; commit `application-example.yml`.
14. **Đổi schema** thì phải sửa cùng lúc `db/AGR05.sql` và `docs/data-dictionary.md`, rồi báo lại cho người dùng (báo cáo Word cũng phải sửa theo).
15. **Lỗi trả về giao diện:** lỗi nghiệp vụ trả HTTP 400/403/409 kèm JSON `{ "loi": "câu tiếng Việt", "truong": "tênÔ" }`; giao diện hiện câu đó và viền đỏ ô sai, không mất dữ liệu đã nhập.

## Kiểm thử

- Mỗi test case trong `docs/test-cases.md` có ít nhất một test JUnit, tên phương thức bắt đầu bằng mã, ví dụ `TC05_duyet_khiChuaChonLoai_baoLoi`.
- Test BLL dùng DAL giả (lớp fake cài interface `I...Dal`, đặt trong thư mục test), để chạy được mà không cần SQL Server.
- Test tích hợp với CSDL thật gắn `@Tag("integration")`, đọc chuỗi kết nối từ biến môi trường `AGR05_TEST_DB`; bỏ qua (`assumeTrue`) nếu biến không có.
- Không sửa test để cho qua. Test sai so với `docs/` thì báo lại.

## Hoàn thành một bước nghĩa là

1. `mvn -q verify` trong `backend/` không lỗi, không cảnh báo mới; nếu bước có frontend thì `npm run build` trong `frontend/` không lỗi.
2. Test qua hết (trừ test integration khi chưa có CSDL).
3. Liệt kê ngắn: đã làm gì, file nào, test nào, và **mọi chỗ code khác với `docs/`** (nếu có) kèm lý do.
4. Không làm việc của bước sau khi chưa được yêu cầu.

## Lệnh hay dùng

```
cd backend && mvn verify                       # build + test (bỏ qua integration)
cd backend && mvn verify -Pintegration         # kèm test tích hợp (cần AGR05_TEST_DB)
cd backend && mvn spring-boot:run              # chạy API ở http://localhost:8080
cd frontend && npm install && npm run dev      # chạy giao diện ở http://localhost:5173
sqlcmd -S . -E -I -f 65001 -i db\AGR05.sql     # tạo CSDL (đổi tên server nếu khác)
```

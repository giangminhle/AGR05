# Câu lệnh cho Claude Code, làm lần lượt từng bước

Cách dùng:
- Mỗi bước dán nguyên một câu lệnh vào Claude Code.
- Chờ xong, chạy thử, rồi commit git (`git add . && git commit -m "Buoc N"`). Sau đó mới sang bước sau.
- Khi chuyển bước, gõ `/clear` để ngữ cảnh gọn. Claude Code vẫn tự đọc lại `CLAUDE.md`.
- Bước nào lớn (bước 5, 6, 9, 10), hãy bảo Claude Code lập kế hoạch trước và đọc kế hoạch rồi mới cho làm.

---

## Bước 0: Đọc và kiểm tra tài liệu (chưa code)

```
Đọc CLAUDE.md và toàn bộ thư mục docs/ cùng db/AGR05.sql. Chưa viết code.
Trả lời: (1) tóm tắt hệ thống trong 10 dòng; (2) danh sách lớp sẽ tạo ở từng gói (entity, dal, bll, api)
và danh sách trang React; (3) mọi chỗ docs/ mâu thuẫn với nhau hoặc với db/AGR05.sql. Chỉ liệt kê, không tự sửa.
```

## Bước 1: Khung project và CSDL

Trước bước này, bạn tự chạy script trên máy: `sqlcmd -S . -E -I -f 65001 -i db\AGR05.sql`. Nếu tên server khác thì sửa lại. Nếu script lỗi, dán thông báo lỗi cho Claude Code để nó sửa `db/AGR05.sql`.

```
Tạo backend/ là project Maven Spring Boot 3.5, Java 21, groupId vn.agr05, artifactId agr05-backend, gói gốc vn.agr05
với các gói entity, dal, bll, api, config đúng CLAUDE.md. Dependency: spring-boot-starter-web, -jdbc, -security,
-validation, mssql-jdbc, smile-core, và để test: spring-boot-starter-test, archunit-junit5.
Tạo application-example.yml (spring.datasource, agr05.thu-muc-anh, agr05.thu-muc-mo-hinh) và cách đọc cấu hình
(@ConfigurationProperties). Cấu hình TransactionTemplate dùng chung. Profile Maven "integration" để chạy test @Tag("integration").
Viết test ArchUnit kiểm tra phụ thuộc một chiều api → bll → dal → entity.
Tạo frontend/ bằng Vite + React (JavaScript), cài react-router-dom, react-bootstrap, bootstrap, recharts;
cấu hình proxy /api → http://localhost:8080. Trang tạm "AGR-05 đang xây dựng".
Thêm .gitignore cho Java, Node (bỏ qua application.yml, target, node_modules, dist, thư mục ảnh và mô hình).
Chạy mvn verify và npm run build.
```

## Bước 2: Entity

```
Đọc docs/domain-model.md và docs/data-dictionary.md. Tạo các lớp entity trong gói vn.agr05.entity cho 12 lớp thực thể
(kiểu Java khớp kiểu SQL: INT → Integer/int, DECIMAL → BigDecimal, DATETIME2 → LocalDateTime, DATE → LocalDate;
cột cho phép NULL thì dùng kiểu đối tượng). Tạo các lớp hằng số TrangThai, VaiTro, LoaiThongBao, MucNguyCo.
Cài các phương thức nghiệp vụ trên entity đúng tên trong docs: NguoiDung.xacThucMatKhau,
BaoCaoHienTruong.kiemTraHopLe(soAnh), chuyenTrangThai(tt) (chặn chuyển sai thứ tự theo BR03 và bảng chuyển trạng thái),
laQuaHan(hanGio), choPhepSua(maNguoiDung, hanNgay), DuBaoNguyCo.laNguyCoCao, MoHinhAI.khongKemHon(mh).
Viết unit test cho các phương thức này, gồm giá trị biên của BR01 (0, 1, 5, 6 ảnh; vĩ độ ±90, 95).
```

## Bước 3: DAL

```
Đọc docs/flows.md và docs/architecture.md, liệt kê mọi hàm DAL được gọi trong các sơ đồ tuần tự.
Cài các lớp ...Dal kèm interface I...Dal bằng JdbcTemplate / NamedParameterJdbcTemplate, chỉ dùng câu lệnh có tham số.
Hàm ghi dữ liệu chạy trong transaction của Service (không tự mở transaction).
updateTrangThai và updateNoiDung phải có điều kiện WHERE MaTrangThai = trạng thái cũ và trả về boolean (số dòng > 0).
Không tạo hàm xóa báo cáo. Viết test tích hợp (@Tag("integration")) cho BaoCaoDal và ThongBaoDal.
```

## Bước 4: Đăng nhập (UC01)

```
Cài AccountService (dangNhap, taoTaiKhoan, khoaTaiKhoan), AppSession (bean phạm vi session), ThamSoService.layGiaTri
và DbSeeder: lần đầu chạy, nếu chưa có tài khoản QUAN_TRI thì tạo tài khoản admin và in mật khẩu ra log.
Băm mật khẩu theo luật 9 trong CLAUDE.md. Cấu hình Spring Security: phiên đăng nhập bằng cookie, phân quyền theo vai trò,
trả 401/403 dạng JSON. Cài AuthController (dang-nhap, dang-xuat, toi) theo bảng API trong docs/architecture.md.
Làm đúng luồng UC01 và sơ đồ tuần tự 3.3.1 trong docs/flows.md: sai tên hoặc sai mật khẩu đều báo cùng một câu;
tài khoản bị khóa báo riêng. Viết test với DAL giả.
```

## Bước 5: Gửi báo cáo (UC02)

```
Đọc UC02 trong docs/spec.md và mục 3.3.2 trong docs/flows.md. Cài AnhStorage, ThongBaoService.taoThongBao
và BaoCaoService.guiBaoCao đúng thứ tự gọi trong sơ đồ: kiểm tra BR01 → kiểm tra file → lưu file → một transaction
(báo cáo, ảnh, lịch sử, thông báo) → Commit; lỗi thì Rollback và xóa file đã lưu.
Cài BaoCaoController POST /api/bao-cao (multipart) và AnhController GET /api/anh/{ma} có kiểm tra quyền.
Viết test TC01, TC02, TC03, TC04.
```

## Bước 6: Xác minh, từ chối, cảnh báo BR06 (UC05–UC10)

```
Đọc UC05–UC10 trong docs/spec.md và các mục 3.3.3, 3.3.4, 3.3.5 trong docs/flows.md.
Cài XacMinhService (layDanhSachCho, layChiTiet, xacMinh, tuChoi, danhDauDaXuLy), CanhBaoService.kiemTraNguong
và XacMinhController. Kiểm tra quyền khu vực và "không xác minh báo cáo của chính mình" (BR05). Từ chối báo cáo đã
gửi lại một lần thì đóng hồ sơ trong cùng transaction (BR04). Chưa gọi AIService ở bước này, để sẵn chỗ gọi sau Commit.
Viết test TC05, TC06, TC07, TC08, TC09.
```

## Bước 7: Sửa, gửi lại, chức năng nền (UC03, UC04, FR-17)

```
Cài BaoCaoService.layCuaToi, layDeSua, guiLai theo UC03 và mục 3.3.8 (kèm API cua-toi, de-sua, PUT).
Cài HanXuLyService.kiemTraQuaHan và dongHoSoHetHan theo mục 3.3.7 (BR02, BR04), đọc thời hạn từ ThamSoService,
và HanXuLyScheduler (@Scheduled 60 giây, bật @EnableScheduling). Viết test TC10 và test cho quá hạn.
```

## Bước 8: Quản trị và thống kê (UC11, UC13–UC16)

```
Cài DanhMucService (loại sâu bệnh, khu vực, phanCong, goPhanCong), quản lý người dùng trong AccountService,
ThamSoService.capNhat (NGUONG_NGUY_CO trong 1..100), ThongKeService.thongKe, ThongBaoService (demChuaDoc,
layThongBao, danhDauDaDoc) cùng các Controller tương ứng. Chuyên gia chỉ thống kê khu vực mình phụ trách.
Viết test TC16 và test "không khóa chính tài khoản mình".
```

## Bước 9: Giao diện React (làm 3 lần)

```
9a. Đọc docs/ui.md. Làm lớp gọi API (src/api), DangNhapPage và MainLayout (menu theo vai trò, nút thông báo có số đếm,
    gọi API đếm chưa đọc mỗi 60 giây, đăng xuất). Tên thành phần đúng bảng trong docs/ui.md.
9b. Làm BaoCaoHienTruongPage (dùng chung gửi mới và sửa), TheoDoiBaoCaoPage, ThongBaoPage.
9c. Làm XacMinhPage, ThongKePage, DanhMucSauBenhPage, KhuVucPage, NguoiDungPage, CauHinhPage.
Trang chỉ gọi API. Lỗi nghiệp vụ hiện bằng câu báo lỗi và viền đỏ ô sai; không để lỗi làm trắng trang.
Chạy npm run build không lỗi.
```

## Bước 10: Mô-đun AI (UC18, UC19)

```
Đọc docs/ai-module.md và các mục 3.3.9, 3.3.10. Cài AIService bằng Smile: taoDacTrung, tạo bảng mẫu (nhãn theo định nghĩa
bùng phát), huanLuyenLai (GradientTreeBoost, chia theo thời gian 80/20, tính Accuracy, F1, AUC, lưu file mô hình và dòng
MoHinhAI DangSuDung = 0), kichHoat (so F1, đổi DangSuDung trong một transaction), duBaoNguyCo (lỗi thì trả null, xếp mức
theo NGUONG_NGUY_CO, gọi CanhBaoService.taoCanhBaoTuDuBao không tạo trùng). Gắn lời gọi duBaoNguyCo sau Commit
trong XacMinhService.xacMinh. Làm MoHinhAIController và MoHinhAIPage (nút khóa và hiện chờ trong lúc huấn luyện).
Viết công cụ tạo DỮ LIỆU GIẢ để thử (ghi rõ trong tên và chú thích là dữ liệu demo, không dùng cho số liệu báo cáo).
Viết test TC11, TC12, TC13, TC14, TC15.
```

## Bước 11: Rà soát cuối

```
Đối chiếu toàn bộ code với docs/: liệt kê lớp, hàm, API có trong thiết kế mà chưa có trong code, hoặc ngược lại,
và mọi chỗ code khác thiết kế. Chạy toàn bộ test, xuất bảng TC01–TC16 với cột Kết quả thực tế (Đạt / Không đạt
và lý do) để mình điền vào mục 7.2 của báo cáo.
```

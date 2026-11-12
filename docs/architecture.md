# Kiến trúc

## 5.1 Kiến trúc hệ thống

### 5.1.1 Framework
Hệ thống là ứng dụng web: cán bộ tại Chi cục và nông dân ở xã dùng trình duyệt để truy cập, không phải cài phần mềm trên từng máy. Đề tài chọn các công nghệ dưới đây vì phổ biến, có hệ sinh thái lớn và có thư viện học máy chạy trên cùng nền Java.

| Thành phần | Công nghệ | Lý do chọn |
|---|---|---|
| Ngôn ngữ, nền tảng backend | Java 21, Spring Boot 3.5 | Kiểu tĩnh, hỗ trợ tốt lập trình hướng đối tượng; Spring Boot cấu hình sẵn web server, bảo mật, transaction. |
| Kiểu giao tiếp | REST API (JSON) | Tách rời giao diện và nghiệp vụ, giao diện chỉ gọi API. |
| Giao diện | React 18 + Vite, Bootstrap 5 | Xây giao diện theo thành phần, chạy trên mọi trình duyệt. |
| Cơ sở dữ liệu | Microsoft SQL Server | Hỗ trợ transaction, ràng buộc CHECK, trigger, index lọc. |
| Truy cập dữ liệu | Spring JDBC (JdbcTemplate) | Câu lệnh SQL có tham số, chủ động điều khiển transaction bằng TransactionTemplate. |
| Bảo mật | Spring Security | Đăng nhập theo phiên, phân quyền theo vai trò ở phía server. |
| Học máy | Smile (GradientTreeBoost) | Huấn luyện và chạy mô hình ngay trong ứng dụng Java, không cần dịch vụ ngoài. |
| Băm mật khẩu | PBKDF2 (PBKDF2WithHmacSHA256) | Có sẵn trong Java, dùng muối riêng cho từng tài khoản (SEC01). |


### 5.1.2 Mô hình kiến trúc hệ thống
Hệ thống theo kiến trúc phân tầng (ARCH01) cộng tầng Entity dùng chung:

- **Tầng trình bày (Presentation):** gồm trang React chạy trên trình duyệt và các REST Controller trong backend. Trang React lấy dữ liệu người dùng và gọi API; Controller nhận request, chuyển sang Service và trả JSON. Cả hai không chứa câu lệnh SQL và không chứa quy tắc nghiệp vụ.
- **Tầng nghiệp vụ (BLL):** các Service kiểm tra quy tắc, quyền, điều khiển transaction, gọi mô hình AI.
- **Tầng truy cập dữ liệu (DAL):** các lớp ...Dal là nơi duy nhất có câu lệnh SQL.
- **Entity:** các đối tượng như BaoCaoHienTruong, ThongBao làm "vật chứa" dữ liệu luân chuyển giữa các tầng và tự kiểm tra các quy tắc chỉ liên quan tới chính nó.

Phụ thuộc chỉ đi một chiều: Controller → Service → DAL → Entity. Trình duyệt chỉ nói chuyện với Controller qua HTTP.

*(Sơ đồ trong báo cáo: Biểu đồ 25. Biểu đồ lớp thiết kế chi tiết)*
Bảng dưới tóm tắt trách nhiệm của các lớp tầng nghiệp vụ. Riêng cho mô-đun AI, tầng BLL có AIService, tầng DAL có DuBaoDal và MoHinhAIDal, tầng Entity có DuBaoNguyCo và MoHinhAI.

| Lớp | Tầng | Trách nhiệm |
|---|---|---|
| AccountService | BLL | Đăng nhập, băm và kiểm tra mật khẩu (SEC01), tạo và khóa tài khoản. |
| BaoCaoService | BLL | Gửi báo cáo, sửa và gửi lại, lấy danh sách báo cáo của nông dân. |
| XacMinhService | BLL | Danh sách chờ, duyệt, từ chối, đánh dấu đã xử lý. Gọi AIService sau khi Commit. |
| AIService | BLL | Tạo đặc trưng, gọi mô hình Smile để dự báo, huấn luyện lại và kích hoạt phiên bản. |
| CanhBaoService | BLL | Kiểm tra ngưỡng BR06, tạo cảnh báo từ dự báo AI (BR08), chặn cảnh báo trùng. |
| ThongBaoService | BLL | Tạo thông báo nội bộ, đếm chưa đọc, đánh dấu đã đọc. |
| HanXuLyService | BLL | Chức năng nền FR-17: gắn cờ quá hạn, đóng hồ sơ hết hạn gửi lại. |
| HanXuLyScheduler | BLL | Lịch chạy nền (`@Scheduled`, 60 giây) gọi HanXuLyService (ARCH04). |
| DanhMucService | BLL | Danh mục sâu bệnh, khu vực, phân công chuyên gia. |
| ThongKeService | BLL | Đếm báo cáo, ca dịch, cảnh báo và dự báo theo kỳ. |
| ThamSoService | BLL | Đọc và cập nhật năm tham số trong ThamSoHeThong. |
| AnhStorage | BLL | Kiểm tra định dạng, dung lượng ảnh; lưu và xóa file ảnh. |
| AppSession | BLL | Người dùng đang đăng nhập và các khu vực phụ trách, lưu trong phiên (session) của Spring. |
| Các lớp ...Controller | Presentation | Nhận HTTP request, gọi Service, trả JSON. Không có nghiệp vụ. |
| Các lớp ...Dal | DAL | Mỗi bảng một lớp DAL, chỉ chứa câu lệnh SQL có tham số. |


## 5.2 Mô tả chi tiết thành phần trong hệ thống
Mỗi chức năng nghiệp vụ được thiết kế một sơ đồ tương tác riêng, thể hiện dòng chảy dữ liệu từ trang web xuống tầng xử lý.


### 5.2.1 Chức năng Gửi báo cáo hiện trường
Nghiệp vụ này hỗ trợ nông dân ghi nhận tình trạng dịch hại. Ràng buộc quan trọng nhất là thông tin báo cáo và hình ảnh đính kèm phải được lưu đồng thời.

*(Sơ đồ trong báo cáo: Biểu đồ 26. Thiết kế chức năng Gửi báo cáo hiện trường)*
Quy trình: Khi người dùng bấm "Gửi" trên trang BaoCaoHienTruongPage, trang gửi request `multipart/form-data` (thông tin báo cáo + các file ảnh) tới BaoCaoController. Controller dựng đối tượng BaoCaoHienTruong và truyền xuống BaoCaoService. Service kiểm tra tọa độ và số lượng ảnh. Nếu thỏa mãn, Service gọi các hàm insert của DAL trong một transaction thống nhất.


### 5.2.2 Chức năng Xác minh báo cáo
Chức năng dành riêng cho chuyên gia phân tích các báo cáo đang "Chờ xác minh", đưa ra kết luận và phản hồi lại cho nông dân.

*(Sơ đồ trong báo cáo: Biểu đồ 27. Thiết kế chức năng Xác minh báo cáo)*
Quy trình: Chuyên gia chọn báo cáo trong bảng, nhập khuyến nghị và nhấn "Duyệt" trên trang XacMinhPage. Trang gọi API `POST /api/xac-minh/{ma}/duyet`. XacMinhController chuyển cho XacMinhService; service kiểm tra quyền xử lý khu vực của chuyên gia. Nếu hợp lệ, service gọi BaoCaoDal để cập nhật trạng thái thành "Đã xác minh", đồng thời gọi ThongBaoDal để tạo thông báo nội bộ cho nông dân. Tất cả các bước lưu trữ này nằm trong một transaction duy nhất.

Sau khi Commit, XacMinhService gọi AIService để dự báo nguy cơ dịch. Phần này trình bày ở mục 5.2.3.


### 5.2.3 Chức năng Dự báo nguy cơ dịch (AI)
Chức năng chạy ngay sau mỗi lần duyệt. AIService không truy cập CSDL trực tiếp mà đi qua các lớp DAL, giống các service khác (ARCH01). Thư viện Smile chỉ được gọi bên trong AIService, nên nếu sau này đổi thuật toán thì các lớp còn lại không phải sửa.

*(Sơ đồ trong báo cáo: Biểu đồ 28. Thiết kế chức năng Dự báo nguy cơ dịch)*
Quy trình: AIService lấy mô hình đang dùng qua MoHinhAIDal, đếm số ca đã xác minh qua BaoCaoDal, tính xác suất, lưu kết quả qua DuBaoDal. Nếu mức Cao thì nhờ CanhBaoService tạo cảnh báo. API duyệt trả kết quả dự báo về, trang XacMinhPage hiện mức nguy cơ ở ô "Nguy cơ dịch" để chuyên gia biết ngay sau khi duyệt.


### 5.2.4 Chức năng Huấn luyện lại mô hình AI
Chức năng dành cho Quản trị viên. Việc huấn luyện chỉ đọc dữ liệu nghiệp vụ và ghi thêm một phiên bản mô hình, không sửa báo cáo nào.

*(Sơ đồ trong báo cáo: Biểu đồ 29. Thiết kế chức năng Huấn luyện lại mô hình AI)*
Quy trình: Trang MoHinhAIPage gọi API huấn luyện. AIService lấy dữ liệu qua BaoCaoDal, dùng Smile để huấn luyện, đánh giá, lưu file mô hình, rồi ghi phiên bản mới qua MoHinhAIDal. Khi Quản trị viên bấm Kích hoạt, AIService so sánh với bản đang dùng rồi mới gọi datDangSuDung.


## Phụ lục A: danh sách REST API

Mọi API nằm dưới `/api`, trả JSON. Cột "Vai trò" là vai trò được phép gọi (Spring Security kiểm tra); quyền chi tiết (khu vực, chủ báo cáo) do Service kiểm tra.

| Phương thức, đường dẫn | Gọi Service | Vai trò | Use case |
|---|---|---|---|
| `POST /api/auth/dang-nhap` | AccountService.dangNhap | Mọi người | UC01 |
| `POST /api/auth/dang-xuat` | (hủy phiên) | Đã đăng nhập | UC01 |
| `GET /api/auth/toi` | AppSession | Đã đăng nhập | UC01 |
| `GET /api/khu-vuc` | DanhMucService | Đã đăng nhập | UC02, UC14 |
| `POST /api/bao-cao` (multipart) | BaoCaoService.guiBaoCao | NONG_DAN | UC02 |
| `GET /api/bao-cao/cua-toi` | BaoCaoService.layCuaToi | NONG_DAN | UC04 |
| `GET /api/bao-cao/{ma}/de-sua` | BaoCaoService.layDeSua | NONG_DAN | UC03 |
| `PUT /api/bao-cao/{ma}` (multipart) | BaoCaoService.guiLai | NONG_DAN | UC03 |
| `GET /api/xac-minh/cho` | XacMinhService.layDanhSachCho | CHUYEN_GIA | UC05 |
| `GET /api/xac-minh/da-xac-minh` | XacMinhService.layDaXacMinh | CHUYEN_GIA | UC10 |
| `GET /api/xac-minh/{ma}` | XacMinhService.layChiTiet | CHUYEN_GIA | UC06 |
| `POST /api/xac-minh/{ma}/duyet` | XacMinhService.xacMinh | CHUYEN_GIA | UC07, UC09, UC18 |
| `POST /api/xac-minh/{ma}/tu-choi` | XacMinhService.tuChoi | CHUYEN_GIA | UC08 |
| `POST /api/xac-minh/{ma}/da-xu-ly` | XacMinhService.danhDauDaXuLy | CHUYEN_GIA | UC10 |
| `GET /api/anh/{maHinhAnh}` | BaoCaoService.layAnh (kiểm tra quyền xem) | Đã đăng nhập | UC04, UC06 |
| `GET /api/thong-bao` | ThongBaoService.layThongBao | Đã đăng nhập | UC12, UC17 |
| `GET /api/thong-bao/chua-doc` | ThongBaoService.demChuaDoc | Đã đăng nhập | UC17 |
| `POST /api/thong-bao/{ma}/da-doc` | ThongBaoService.danhDauDaDoc | Đã đăng nhập | UC17 |
| `GET /api/thong-ke` | ThongKeService.thongKe | CHUYEN_GIA, QUAN_TRI | UC11 |
| `GET, POST, PUT /api/loai-sau-benh` | DanhMucService.luuLoaiSauBenh | QUAN_TRI (GET: đã đăng nhập) | UC13 |
| `DELETE /api/loai-sau-benh/{ma}` | DanhMucService.xoaLoaiSauBenh | QUAN_TRI | UC13 |
| `POST, PUT /api/khu-vuc` | DanhMucService.luuKhuVuc | QUAN_TRI | UC14 |
| `GET /api/khu-vuc/{ma}/chuyen-gia` | DanhMucService.layChuyenGiaCuaKhuVuc | QUAN_TRI | UC14 |
| `POST /api/khu-vuc/{ma}/phan-cong` | DanhMucService.phanCong | QUAN_TRI | UC14 |
| `DELETE /api/khu-vuc/{ma}/phan-cong/{maCG}` | DanhMucService.goPhanCong | QUAN_TRI | UC14 |
| `GET, POST /api/nguoi-dung` | AccountService.taoTaiKhoan | QUAN_TRI | UC15 |
| `PUT /api/nguoi-dung/{ma}` | AccountService.capNhat (họ tên, điện thoại, vai trò) | QUAN_TRI | UC15 |
| `POST /api/nguoi-dung/{ma}/khoa` | AccountService.khoaTaiKhoan | QUAN_TRI | UC15 |
| `GET /api/tham-so`, `PUT /api/tham-so/{ma}` | ThamSoService.capNhat | QUAN_TRI | UC16 |
| `GET /api/mo-hinh-ai` | MoHinhAIDal qua AIService | QUAN_TRI | UC19 |
| `POST /api/mo-hinh-ai/huan-luyen` | AIService.huanLuyenLai | QUAN_TRI | UC19 |
| `POST /api/mo-hinh-ai/{ma}/kich-hoat` | AIService.kichHoat | QUAN_TRI | UC19 |

## Phụ lục B: danh sách lớp và hàm (lấy từ biểu đồ lớp thiết kế và sơ đồ tuần tự)

Tên dưới đây là bắt buộc. Có thể thêm hàm phụ trợ private, nhưng không đổi tên hàm public.

### Tầng trình bày

- **Trang React (frontend/src/pages):** DangNhapPage, MainLayout, BaoCaoHienTruongPage, TheoDoiBaoCaoPage, XacMinhPage, ThongBaoPage, ThongKePage, DanhMucSauBenhPage, KhuVucPage, NguoiDungPage, CauHinhPage, MoHinhAIPage
- **REST Controller (backend, gói api):** AuthController, BaoCaoController, XacMinhController, AnhController, ThongBaoController, ThongKeController, DanhMucController, NguoiDungController, ThamSoController, MoHinhAIController

### Tầng BLL

**AccountService**

- `+dangNhap(ten, matKhau) : NguoiDung`
- `+taoTaiKhoan(nd, matKhau) : int`
- `+khoaTaiKhoan(ma, khoa) : boolean`
- `+capNhat(nd) : boolean`

**BaoCaoService**

- `+guiBaoCao(bc, dsFileAnh) : int`
- `+layCuaToi(maNguoiDung) : List<BaoCaoHienTruong>`
- `+layDeSua(ma, maNguoiDung) : BaoCaoHienTruong`
- `+guiLai(bc, dsMoi, dsXoa) : boolean`
- `+layAnh(maHinhAnh, nguoiXem, dsKhuVuc) : AnhTai`

**XacMinhService**

- `+layDanhSachCho(maCG) : List<BaoCaoHienTruong>`
- `+layDaXacMinh(maCG) : List<BaoCaoHienTruong>`
- `+layChiTiet(ma, maCG) : BaoCaoHienTruong` (thêm maCG để kiểm tra khu vực phụ trách, BR05)
- `+xacMinh(ma, maLoai, kn, maCG) : boolean`
- `+tuChoi(ma, lyDo, maCG) : boolean`
- `+danhDauDaXuLy(ma, ghiChu, maCG) : boolean`

**AIService**

- `+duBaoNguyCo(bc) : DuBaoNguyCo`
- `+huanLuyenLai() : MoHinhAI`
- `+kichHoat(maMoHinh) : boolean`
- `-taoDacTrung(bc) : DacTrung`
- `-xepMuc(xacSuat, nguong) : String`

**CanhBaoService**

- `+kiemTraNguong(maKV, maLoai) : void` (chạy bên trong transaction đang mở của người gọi)
- `+taoCanhBaoTuDuBao(duBao, bc) : Integer`

**ThongBaoService**

- `+taoThongBao(maNhan, loai, maBaoCao, tieuDe, noiDung) : int` (chạy bên trong transaction đang mở của người gọi)
- `+demChuaDoc(ma) : int`
- `+layThongBao(ma) : List<ThongBao>`
- `+danhDauDaDoc(maTB, ma) : boolean`

**HanXuLyService**

- `+kiemTraQuaHan() : int`
- `+dongHoSoHetHan() : int`

**DanhMucService**

- `+luuLoaiSauBenh(loai) : int`
- `+xoaLoaiSauBenh(maLoai) : boolean`
- `+luuKhuVuc(kv) : int`
- `+phanCong(maCG, maKV) : boolean`
- `+goPhanCong(maCG, maKV) : boolean`

**ThongKeService**

- `+thongKe(tu, den, maKV, maLoai) : BangThongKe`

**ThamSoService**

- `+layGiaTri(ma) : int`
- `+capNhat(ma, giaTri) : boolean`

**AnhStorage**

- `+kiemTraFile(file) : boolean`
- `+luu(file) : String`
- `+xoa(duongDan) : void`

- **AppSession**: lưu người dùng đang đăng nhập và danh sách khu vực phụ trách (`datNguoiDung(nd, dsMaKhuVuc)`), phạm vi phiên (session scope).
- **DbSeeder**: tạo tài khoản quản trị đầu tiên khi ứng dụng khởi động.
- **HanXuLyScheduler**: mỗi 60 giây gọi `kiemTraQuaHan` và `dongHoSoHetHan`.

### Tầng DAL

Transaction do Service mở bằng TransactionTemplate; các hàm DAL dùng chung kết nối của transaction đang mở nên không nhận tham số transaction.

- **NguoiDungDal**: `getByTenDangNhap`, `getKhuVucPhuTrach`, cùng các hàm thêm, sửa, khóa tài khoản
- **KhuVucDal**: các hàm CRUD cần cho use case
- **LoaiSauBenhDal**: các hàm CRUD cần cho use case
- **BaoCaoDal**: `danhDauQuaHan`, `demCaDaXacMinh`, `getBiBacBoHetHan`, `getById`, `getByTrangThai`, `getDaXacMinh`, `getQuaHan`, `insert`, `updateNoiDung`, `updateTrangThai` (không có hàm xóa báo cáo)
- **HinhAnhDal**: `getByBaoCao`, `getById`, `insert`, `delete` (xóa ảnh khi nông dân bỏ ảnh lúc sửa, gửi lại)
- **LichSuDal**: `getByBaoCao`, `insert`
- **ThongBaoDal**: `insert`, `countChuaDoc`, `getByNguoiNhan`, `markRead`
- **CanhBaoDal**: các hàm CRUD cần cho use case
- **ThamSoDal**: các hàm CRUD cần cho use case
- **DuBaoDal**: `insert`, `getByBaoCao`, `demTheoMuc`
- **MoHinhAIDal**: `datDangSuDung`, `getDangSuDung`, `insert`

### Tầng Entity

- **NguoiDung**: `+xacThucMatKhau(mk) : boolean`
- **BaoCaoHienTruong**: `+kiemTraHopLe(soAnh) : String`, `+chuyenTrangThai(tt) : void`, `+laQuaHan(hanGio) : boolean`, `+choPhepSua(maND, hanNgay) : boolean`
- **DuBaoNguyCo**: `+laNguyCoCao() : boolean`
- **MoHinhAI**: `+khongKemHon(mh) : boolean`
- **KhuVuc**
- **PhanCongKhuVuc**
- **LoaiSauBenh**
- **HinhAnh**
- **LichSuXacMinh**
- **CanhBao**
- **ThongBao**
- **ThamSoHeThong**

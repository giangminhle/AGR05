# Từ điển dữ liệu

Script chạy được nằm ở db/AGR05.sql (đã sửa: cột TrieuChung, CanhBao.MaDuBao, CHECK NGUONG_NGUY_CO).

## 4.1 Sơ đồ logic
Có 15 bảng. Hai bảng MoHinhAI và DuBaoNguyCo phục vụ mô-đun AI. Trạng thái "Khởi tạo" chỉ có trên form, chưa lưu CSDL, nên bảng TrangThaiBaoCao chỉ có 4 giá trị.

*(Sơ đồ trong báo cáo: Biểu đồ 24. Sơ đồ logic (ERD))*
Ký hiệu đầu đường nối theo kiểu chân chim: hai gạch là đúng một, vòng tròn và gạch là không hoặc một, vòng tròn và chân chim là không hoặc nhiều, gạch và chân chim là một hoặc nhiều. Hai đường TrangThaiBaoCao – LichSuXacMinh ứng với trạng thái mới và trạng thái cũ.

Đọc sơ đồ

BaoCaoHienTruong là bảng trung tâm, nối với 4 bảng cha (NguoiDung, KhuVuc, LoaiSauBenh, TrangThaiBaoCao) và 4 bảng con (HinhAnh, LichSuXacMinh, ThongBao, DuBaoNguyCo).

MaLoai trong BaoCaoHienTruong cho phép NULL vì lúc mới gửi chưa phân loại.

MaNguoiThucHien trong LichSuXacMinh cho phép NULL. NULL nghĩa là hệ thống tự làm (đóng hồ sơ hết hạn).

ThamSoHeThong đứng riêng, không nối với bảng nào (quy tắc 9).

DuBaoNguyCo nối với BaoCaoHienTruong theo quan hệ 1 – 0..1 (UQ MaBaoCao) và với MoHinhAI theo quan hệ 1 – n. CanhBao.MaDuBao cho phép NULL vì cảnh báo theo BR06 không gắn với dự báo nào.

Quy tắc chuyển đổi đã áp dụng

| Quy tắc | Áp dụng trong đề tài |
|---|---|
| 1. Lớp thành bảng | Mỗi lớp trong mục 3.1 thành một bảng. Mỗi thuộc tính thành một cột. |
| 2. Liên kết 1-1 | Cài đặt bằng khóa ngoại + UNIQUE: DuBaoNguyCo.MaBaoCao (1 – 0..1 với báo cáo) và CanhBao.MaDuBao (0..1 – 0..1, UNIQUE có lọc WHERE MaDuBao IS NOT NULL). |
| 3. Liên kết 1-n | Khóa của bên "1" chuyển sang bảng bên "n". Ví dụ MaNguoiGui, MaKhuVuc, MaLoai nằm trong BaoCaoHienTruong. HinhAnh, LichSuXacMinh, ThongBao, CanhBao cũng làm theo cách này. |
| 4. Liên kết n-n | NguoiDung và KhuVuc (chuyên gia phụ trách khu vực) thành bảng trung gian PhanCongKhuVuc, khóa chính là cặp (MaNguoiDung, MaKhuVuc). |
| 5. Kế thừa | Nông dân, Chuyên gia, Quản trị viên dùng cách 2: gộp vào một bảng NguoiDung và thêm cột MaVaiTro. Ba vai trò dùng chung thuộc tính, không có thuộc tính riêng. |
| 6. Thuộc tính phức hợp | Tọa độ tách thành hai cột ViDo và KinhDo. |
| 7. Thuộc tính mảng | Danh sách ảnh của một báo cáo tách thành bảng HinhAnh. |
| 8. Giá trị rời rạc | Vai trò, trạng thái báo cáo, loại thông báo thành 3 bảng danh mục: VaiTro, TrangThaiBaoCao, LoaiThongBao. |
| 9. Tham số hệ thống | ThamSoHeThong lưu thời hạn xác minh, ngưỡng cảnh báo, số ngày xét ngưỡng, hạn gửi lại và ngưỡng nguy cơ của mô hình AI. |


## 4.2 Mô tả chi tiết các thành phần trong sơ đồ logic
Quy ước: PK khóa chính, FK khóa ngoại, UQ duy nhất.


### 4.2.1 VaiTro
| Tên trường | Kiểu dữ liệu | Khóa | Null | Diễn giải |
|---|---|---|---|---|
| MaVaiTro | VARCHAR(20) | PK | Không | NONG_DAN, CHUYEN_GIA, QUAN_TRI |
| TenVaiTro | NVARCHAR(50) |  | Không | Tên hiển thị của vai trò |


### 4.2.2 NguoiDung
| Tên trường | Kiểu dữ liệu | Khóa | Null | Diễn giải |
|---|---|---|---|---|
| MaNguoiDung | INT IDENTITY | PK | Không | Mã người dùng, tự tăng |
| TenDangNhap | VARCHAR(50) | UQ | Không | Tên đăng nhập, không trùng |
| MatKhauHash | VARCHAR(100) |  | Không | Mật khẩu băm PBKDF2 (SEC01) |
| Muoi | VARCHAR(48) |  | Không | Salt ngẫu nhiên của từng tài khoản |
| HoTen | NVARCHAR(100) |  | Không | Họ tên |
| DienThoai | VARCHAR(15) |  | Có | Số điện thoại, chỉ gồm chữ số |
| MaVaiTro | VARCHAR(20) | FK → VaiTro | Không | Vai trò của tài khoản |
| BiKhoa | BIT |  | Không | 1 = tài khoản bị khóa. Mặc định 0 |
| NgayTao | DATETIME2(0) |  | Không | Ngày tạo tài khoản |


### 4.2.3 KhuVuc
| Tên trường | Kiểu dữ liệu | Khóa | Null | Diễn giải |
|---|---|---|---|---|
| MaKhuVuc | INT IDENTITY | PK | Không | Mã khu vực |
| TenKhuVuc | NVARCHAR(100) | UQ | Không | Tên khu vực |
| MoTa | NVARCHAR(255) |  | Có | Mô tả thêm |


### 4.2.4 PhanCongKhuVuc (bảng trung gian n-n)
| Tên trường | Kiểu dữ liệu | Khóa | Null | Diễn giải |
|---|---|---|---|---|
| MaNguoiDung | INT | PK, FK → NguoiDung | Không | Chuyên gia được giao |
| MaKhuVuc | INT | PK, FK → KhuVuc | Không | Khu vực được giao |
| NgayPhanCong | DATETIME2(0) |  | Không | Ngày giao |

Khóa chính là cặp (MaNguoiDung, MaKhuVuc), nên một chuyên gia không bị giao hai lần vào cùng một khu vực.


### 4.2.5 LoaiSauBenh
| Tên trường | Kiểu dữ liệu | Khóa | Null | Diễn giải |
|---|---|---|---|---|
| MaLoai | INT IDENTITY | PK | Không | Mã loại sâu bệnh |
| TenLoai | NVARCHAR(100) | UQ | Không | Tên loại sâu bệnh |
| TrieuChung | NVARCHAR(1000) |  | Có | Triệu chứng nhận biết |
| BienPhapPhongTru | NVARCHAR(1000) |  | Có | Biện pháp phòng trừ |


### 4.2.6 TrangThaiBaoCao
| Tên trường | Kiểu dữ liệu | Khóa | Null | Diễn giải |
|---|---|---|---|---|
| MaTrangThai | VARCHAR(20) | PK | Không | CHO_XAC_MINH, DA_XAC_MINH, BI_BAC_BO, DA_XU_LY |
| TenTrangThai | NVARCHAR(50) |  | Không | Tên hiển thị |
| ThuTu | TINYINT |  | Không | Thứ tự trong vòng đời (1 đến 4) |


### 4.2.7 BaoCaoHienTruong
| Tên trường | Kiểu dữ liệu | Khóa | Null | Diễn giải |
|---|---|---|---|---|
| MaBaoCao | INT IDENTITY | PK | Không | Mã báo cáo |
| TieuDe | NVARCHAR(200) |  | Không | Tiêu đề báo cáo |
| TenCayTrong | NVARCHAR(100) |  | Không | Cây trồng bị sâu bệnh |
| MoTa | NVARCHAR(1000) |  | Không | Mô tả triệu chứng |
| MucDo | TINYINT |  | Không | 1 = nhẹ, 2 = trung bình, 3 = nặng |
| ViDo | DECIMAL(9,6) |  | Không | Vĩ độ (Lat), từ -90 đến 90 |
| KinhDo | DECIMAL(9,6) |  | Không | Kinh độ (Long), từ -180 đến 180 |
| MaTrangThai | VARCHAR(20) | FK → TrangThaiBaoCao | Không | Trạng thái hiện tại. Mặc định CHO_XAC_MINH |
| KhuyenNghi | NVARCHAR(1000) |  | Có | Khuyến nghị xử lý của chuyên gia |
| NgayPhatHien | DATE |  | Không | Ngày nông dân phát hiện |
| NgayGui | DATETIME2(0) |  | Không | Thời điểm gửi báo cáo lần đầu |
| NgayVaoTrangThai | DATETIME2(0) |  | Không | Thời điểm vào trạng thái hiện tại, để tính hạn 48 giờ và 7 ngày |
| SoLanGuiLai | TINYINT |  | Không | Số lần đã gửi lại (0 hoặc 1). Mặc định 0 |
| QuaHan | BIT |  | Không | 1 = đã gắn cờ quá hạn. Mặc định 0 |
| MaNguoiGui | INT | FK → NguoiDung | Không | Nông dân gửi báo cáo |
| MaKhuVuc | INT | FK → KhuVuc | Không | Khu vực của báo cáo |
| MaLoai | INT | FK → LoaiSauBenh | Có | Loại sâu bệnh do chuyên gia chọn |


### 4.2.8 HinhAnh
| Tên trường | Kiểu dữ liệu | Khóa | Null | Diễn giải |
|---|---|---|---|---|
| MaHinhAnh | INT IDENTITY | PK | Không | Mã ảnh |
| MaBaoCao | INT | FK → BaoCaoHienTruong | Không | Báo cáo chứa ảnh |
| DuongDan | NVARCHAR(260) |  | Không | Đường dẫn (path) tới file ảnh trong thư mục của ứng dụng. CSDL không lưu nội dung ảnh |
| NgayTai | DATETIME2(0) |  | Không | Thời điểm tải ảnh lên |


### 4.2.9 LichSuXacMinh
| Tên trường | Kiểu dữ liệu | Khóa | Null | Diễn giải |
|---|---|---|---|---|
| MaLichSu | INT IDENTITY | PK | Không | Mã dòng lịch sử |
| MaBaoCao | INT | FK → BaoCaoHienTruong | Không | Báo cáo bị đổi trạng thái |
| MaNguoiThucHien | INT | FK → NguoiDung | Có | Người làm. NULL = hệ thống tự làm |
| MaTrangThaiCu | VARCHAR(20) | FK → TrangThaiBaoCao | Có | Trạng thái trước. NULL ở dòng đầu tiên khi mới gửi |
| MaTrangThaiMoi | VARCHAR(20) | FK → TrangThaiBaoCao | Không | Trạng thái sau |
| GhiChu | NVARCHAR(500) |  | Có | Ghi chú hoặc lý do từ chối |
| ThoiGian | DATETIME2(0) |  | Không | Thời điểm đổi trạng thái |


### 4.2.10 CanhBao
| Tên trường | Kiểu dữ liệu | Khóa | Null | Diễn giải |
|---|---|---|---|---|
| MaCanhBao | INT IDENTITY | PK | Không | Mã cảnh báo |
| MaKhuVuc | INT | FK → KhuVuc | Không | Khu vực có dịch |
| MaLoai | INT | FK → LoaiSauBenh | Không | Loại sâu bệnh gây dịch |
| NoiDung | NVARCHAR(500) |  | Không | Nội dung cảnh báo |
| NgayTao | DATETIME2(0) |  | Không | Thời điểm tạo cảnh báo |
| MaDuBao | INT | FK → DuBaoNguyCo, UQ | Có | Dự báo đã sinh ra cảnh báo; NULL nếu cảnh báo theo BR06 |


### 4.2.11 LoaiThongBao
| Tên trường | Kiểu dữ liệu | Khóa | Null | Diễn giải |
|---|---|---|---|---|
| MaLoaiTB | VARCHAR(20) | PK | Không | BAO_CAO_MOI, KET_QUA_XAC_MINH, QUA_HAN, DONG_HO_SO, CANH_BAO_DICH |
| TenLoaiTB | NVARCHAR(50) |  | Không | Tên hiển thị |


### 4.2.12 ThongBao
| Tên trường | Kiểu dữ liệu | Khóa | Null | Diễn giải |
|---|---|---|---|---|
| MaThongBao | INT IDENTITY | PK | Không | Mã thông báo |
| MaNguoiNhan | INT | FK → NguoiDung | Không | Người nhận |
| MaBaoCao | INT | FK → BaoCaoHienTruong | Có | Báo cáo liên quan (nếu có) |
| MaLoaiTB | VARCHAR(20) | FK → LoaiThongBao | Không | Loại thông báo |
| TieuDe | NVARCHAR(200) |  | Không | Tiêu đề |
| NoiDung | NVARCHAR(1000) |  | Không | Nội dung |
| NgayTao | DATETIME2(0) |  | Không | Thời điểm tạo |
| DaDoc | BIT |  | Không | 1 = đã đọc. Mặc định 0 |


### 4.2.13 ThamSoHeThong
| Tên trường | Kiểu dữ liệu | Khóa | Null | Diễn giải |
|---|---|---|---|---|
| MaThamSo | VARCHAR(50) | PK | Không | HAN_XAC_MINH_GIO, NGUONG_CANH_BAO, SO_NGAY_XET_NGUONG, HAN_GUI_LAI_NGAY, NGUONG_NGUY_CO |
| GiaTri | INT |  | Không | Giá trị tham số, phải lớn hơn 0 |
| MoTa | NVARCHAR(255) |  | Có | Ý nghĩa tham số |


### 4.2.14 DuBaoNguyCo
| Tên trường | Kiểu dữ liệu | Khóa | Null | Diễn giải |
|---|---|---|---|---|
| MaDuBao | INT IDENTITY | PK | Không | Mã dự báo |
| MaBaoCao | INT | FK → BaoCaoHienTruong, UQ | Không | Báo cáo đã kích hoạt dự báo |
| MaMoHinh | INT | FK → MoHinhAI | Không | Phiên bản mô hình đã dùng |
| XacSuat | DECIMAL(5,4) |  | Không | 0 đến 1 |
| MucNguyCo | VARCHAR(10) |  | Không | THAP, TRUNG_BINH, CAO |
| SoNgayDuBao | TINYINT |  | Không | Mặc định 7 |
| NgayDuBao | DATETIME2(0) |  | Không | Thời điểm dự báo |


### 4.2.15 MoHinhAI
| Tên trường | Kiểu dữ liệu | Khóa | Null | Diễn giải |
|---|---|---|---|---|
| MaMoHinh | INT IDENTITY | PK | Không | Mã mô hình |
| PhienBan | VARCHAR(20) | UQ | Không | Ví dụ v1, v2 |
| DuongDanFile | NVARCHAR(260) |  | Không | File mô hình Smile đã lưu |
| NgayHuanLuyen | DATETIME2(0) |  | Không |  |
| SoMauHuanLuyen | INT |  | Không | Số mẫu dùng để huấn luyện |
| Accuracy | DECIMAL(5,4) |  | Không | Đo trên tập kiểm tra |
| F1 | DECIMAL(5,4) |  | Không | Dùng để so sánh phiên bản (ARCH05) |
| AUC | DECIMAL(5,4) |  | Không | Đo trên tập kiểm tra |
| DangSuDung | BIT |  | Không | Mặc định 0. Index lọc bảo đảm chỉ một dòng bằng 1 |


## 4.3 Ràng buộc quan trọng
| Mã | Ràng buộc | Cài đặt | Quy tắc liên quan |
|---|---|---|---|
| RB01 | Tọa độ đúng miền giá trị. ViDo từ -90 đến 90, KinhDo từ -180 đến 180. MucDo từ 1 đến 3. Ngày phát hiện không sau ngày gửi. | CHECK trong BaoCaoHienTruong | BR01 |
| RB02 | Mỗi báo cáo có tối đa 5 ảnh. Tối thiểu 1 ảnh do BLL kiểm tra, vì lúc INSERT báo cáo thì chưa có ảnh nào. Ba lệnh INSERT chạy trong một transaction. | TRIGGER trên HinhAnh (tối đa) và BaoCaoService (tối thiểu) | BR01 |
| RB03 | Trạng thái chỉ nhận 4 giá trị hợp lệ nhờ khóa ngoại tới TrangThaiBaoCao. Báo cáo DA_XAC_MINH bắt buộc có MaLoai. Dòng lịch sử chuyển sang BI_BAC_BO bắt buộc có GhiChu (lý do). Thứ tự chuyển trạng thái do BaoCaoHienTruong.chuyenTrangThai kiểm tra. | FOREIGN KEY và CHECK | BR03, BR04 |
| RB04 | Chỉ gửi lại tối đa một lần. SoLanGuiLai chỉ nhận 0 hoặc 1. | CHECK | BR04 |
| RB05 | Không trùng dữ liệu. TenDangNhap, TenKhuVuc, TenLoai là duy nhất. Cặp (MaNguoiDung, MaKhuVuc) trong PhanCongKhuVuc là khóa chính. Một ảnh không được lưu hai lần trong cùng báo cáo. | UNIQUE, PRIMARY KEY | BR05 |
| RB06 | Không xóa báo cáo. Khóa ngoại từ LichSuXacMinh và ThongBao tới BaoCaoHienTruong không có ON DELETE CASCADE. Chỉ HinhAnh xóa theo báo cáo vì ảnh là thành phần của báo cáo (composition). DAL không có hàm xóa báo cáo. | FOREIGN KEY | BR03 |
| RB07 | Tham số phải dương. GiaTri trong ThamSoHeThong lớn hơn 0. | CHECK | ARCH03 |
| RB08 | Chỉ chuyên gia mới được phân công khu vực và chỉ chuyên gia đúng khu vực mới duyệt. SQL Server không CHECK được qua bảng khác một cách gọn, nên các điều kiện này do BLL kiểm tra (BR05). | DanhMucService.phanCong, XacMinhService | BR05 |

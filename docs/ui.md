# Giao diện (web React)

Ảnh khung giao diện nằm trong báo cáo (Hình 1–10). Bố cục và tên thành phần dưới đây là bắt buộc.

# CHƯƠNG VI: THIẾT KẾ GIAO DIỆN

## 6.1 Danh sách các màn hình
Hệ thống có 12 màn hình (trang web). Mục 6.2 mô tả 10 màn hình; DanhMucSauBenhPage và NguoiDungPage có bố cục giống KhuVucPage nên không vẽ riêng.

| STT | Màn hình | Trang React | Đường dẫn | Người dùng | Use case |
|---|---|---|---|---|---|
| 1 | Đăng nhập | DangNhapPage | `/dang-nhap` | Tất cả | UC01 |
| 2 | Khung chính | MainLayout | (bao quanh mọi trang) | Tất cả | UC17, FR-17 |
| 3 | Gửi và sửa báo cáo | BaoCaoHienTruongPage | `/bao-cao/moi`, `/bao-cao/:ma/sua` | Nông dân | UC02, UC03 |
| 4 | Báo cáo của tôi | TheoDoiBaoCaoPage | `/bao-cao/cua-toi` | Nông dân | UC04 |
| 5 | Xác minh báo cáo | XacMinhPage | `/xac-minh` | Chuyên gia | UC05 đến UC10, UC18 |
| 6 | Thông báo | ThongBaoPage | `/thong-bao` | Tất cả | UC12, UC17 |
| 7 | Thống kê và cảnh báo | ThongKePage | `/thong-ke` | Chuyên gia, Quản trị viên | UC11 |
| 8 | Danh mục sâu bệnh | DanhMucSauBenhPage | `/quan-tri/sau-benh` | Quản trị viên | UC13 |
| 9 | Khu vực và phân công | KhuVucPage | `/quan-tri/khu-vuc` | Quản trị viên | UC14 |
| 10 | Người dùng | NguoiDungPage | `/quan-tri/nguoi-dung` | Quản trị viên | UC15 |
| 11 | Cấu hình hệ thống | CauHinhPage | `/quan-tri/cau-hinh` | Quản trị viên | UC16 |
| 12 | Mô hình AI | MoHinhAIPage | `/quan-tri/mo-hinh-ai` | Quản trị viên | UC19 |

Quy ước chung: giao diện tiếng Việt, dùng Bootstrap 5; nhãn đặt phía trên ô nhập, nút chính ở góc dưới bên phải, ô bắt buộc có dấu (*). Khi API trả lỗi, ô sai có viền đỏ (`is-invalid`) kèm dòng báo lỗi bên dưới, trang không tải lại và không mất dữ liệu đã nhập. Thao tác thành công hiện thông báo nhỏ (toast). Trang chỉ hiện mục phù hợp với vai trò; truy cập đường dẫn không đúng vai trò thì chuyển về trang chính. Kiểm tra quyền thật do backend làm.


## 6.2 Mô tả chi tiết các màn hình

### 6.2.1 DangNhapPage: Đăng nhập
Trang đầu tiên khi chưa đăng nhập. Thông báo lỗi dùng chung một câu cho sai tên và sai mật khẩu để không lộ tài khoản nào tồn tại.

*(Sơ đồ trong báo cáo: Hình 1. Giao diện Đăng nhập)*
| Thành phần | Kiểu | Chức năng |
|---|---|---|
| tenDangNhap | Ô nhập văn bản | Tên đăng nhập, tối đa 50 ký tự. |
| matKhau | Ô nhập mật khẩu | Mật khẩu, hiện dấu •. |
| thongBaoLoi | Khung báo lỗi | Hiện lỗi đăng nhập hoặc tài khoản bị khóa. |
| nutDangNhap | Nút | Gọi `POST /api/auth/dang-nhap`. Phím Enter cũng gửi form. |


### 6.2.2 MainLayout: Khung chính
Khung bao quanh mọi trang sau khi đăng nhập. Menu dựng theo vai trò trả về từ `GET /api/auth/toi`, mục không thuộc vai trò thì ẩn hẳn.

*(Sơ đồ trong báo cáo: Hình 2. Giao diện Màn hình chính)*
| Thành phần | Kiểu | Chức năng |
|---|---|---|
| menuChinh | Thanh menu (Navbar) | Các chức năng theo vai trò. |
| nutThongBao | Nút có số đếm (badge) | Hiện số thông báo chưa đọc, bấm để mở ThongBaoPage. |
| hẹn giờ thông báo | `setInterval` 60 giây | Gọi `GET /api/thong-bao/chua-doc` để cập nhật số chưa đọc (ARCH04). Việc kiểm tra quá hạn và đóng hồ sơ (FR-17) chạy phía server bằng HanXuLyScheduler. |
| vungNoiDung | Khung nội dung | Hiện trang con theo đường dẫn. |
| thanhTrangThai | Chân trang | Tên người dùng, vai trò, nút Đăng xuất. |


### 6.2.3 BaoCaoHienTruongPage: Gửi và sửa báo cáo
Trang dùng chung cho gửi mới (UC02) và sửa, gửi lại (UC03). Khi sửa, trang nạp sẵn dữ liệu cũ và ảnh cũ.

*(Sơ đồ trong báo cáo: Hình 3. Giao diện Gửi và sửa báo cáo)*
| Thành phần | Kiểu | Chức năng |
|---|---|---|
| tieuDe, tenCayTrong | Ô nhập văn bản | Bắt buộc. |
| maKhuVuc | Danh sách chọn | Danh sách khu vực. |
| mucDo | Danh sách chọn | Mức độ 1, 2, 3. |
| ngayPhatHien | Ô chọn ngày | Bắt buộc, không sau ngày hôm nay. |
| viDo, kinhDo | Ô nhập số | Giới hạn -90..90 và -180..180 (BR01). |
| moTa | Ô nhập nhiều dòng | Mô tả hiện trường, bắt buộc. |
| khungAnh | Lưới ảnh thu nhỏ | Hiện 1 đến 5 ảnh xem trước, mỗi ảnh có nút bỏ. |
| nutChonAnh | Ô chọn file | Chỉ nhận .jpg, .png; báo lỗi ngay nếu quá 5 MB hoặc quá 5 ảnh. |
| nutGui | Nút | Gọi `POST /api/bao-cao` (mới) hoặc `PUT /api/bao-cao/{ma}` (sửa, gửi lại). |
| nutHuy | Nút | Hỏi xác nhận rồi quay lại, không lưu (UC02-A5). |


### 6.2.4 TheoDoiBaoCaoPage: Báo cáo của tôi
Nông dân xem trạng thái và kết quả xác minh. Nút Sửa và Gửi lại chỉ bật khi báo cáo đang ở trạng thái cho phép (BR03, BR04).

*(Sơ đồ trong báo cáo: Hình 4. Giao diện Báo cáo của tôi)*
| Thành phần | Kiểu | Chức năng |
|---|---|---|
| locTrangThai | Danh sách chọn | Lọc theo trạng thái. |
| bangBaoCao | Bảng | Danh sách báo cáo của người đang đăng nhập (BR05). |
| ketQua | Khung chỉ đọc | Khuyến nghị hoặc lý do bác bỏ, ảnh và lịch sử của báo cáo đang chọn. |
| nutSua, nutGuiLai | Nút | Mở BaoCaoHienTruongPage ở chế độ sửa (UC03). |


### 6.2.5 XacMinhPage: Xác minh báo cáo
Trang chính của chuyên gia. Bên trái là danh sách chờ, bên phải là chi tiết, phía dưới là phần kết luận. Sau khi duyệt, ô "Nguy cơ dịch" hiện kết quả dự báo của mô hình AI.

*(Sơ đồ trong báo cáo: Hình 5. Giao diện Xác minh báo cáo)*
| Thành phần | Kiểu | Chức năng |
|---|---|---|
| locKhuVuc, locMucDo | Danh sách chọn | Lọc danh sách chờ (UC05). |
| bangBaoCao | Bảng | Báo cáo Chờ xác minh trong khu vực phụ trách; dòng quá hạn in đậm, tô đỏ. |
| anhLon, anhNho1..5 | Ảnh | Ảnh lớn và ảnh thu nhỏ, lấy qua `GET /api/anh/{ma}`. |
| bangLichSu | Bảng | Lịch sử xử lý của báo cáo. |
| maLoai | Danh sách chọn | Chọn loại; chọn xong thì ô bienPhap hiện biện pháp phòng trừ. |
| bienPhap | Khung chỉ đọc | Triệu chứng và biện pháp phòng trừ của loại đang chọn. |
| khuyenNghi | Ô nhập nhiều dòng | Khuyến nghị khi duyệt hoặc lý do khi từ chối. |
| nguyCo | Nhãn | Mức và xác suất nguy cơ dịch sau khi duyệt. |
| nutDuyet, nutTuChoi, nutDaXuLy | Nút | Gọi API duyệt, từ chối, đã xử lý. |


### 6.2.6 ThongBaoPage: Thông báo
Danh sách thông báo của người đang đăng nhập. Dòng chưa đọc in đậm.

*(Sơ đồ trong báo cáo: Hình 6. Giao diện Thông báo)*
| Thành phần | Kiểu | Chức năng |
|---|---|---|
| bangThongBao | Bảng | Ngày, loại, tiêu đề. |
| noiDung | Khung chỉ đọc | Nội dung thông báo đang chọn. |
| nutMoBaoCao | Nút | Mở báo cáo gắn với thông báo (nếu có). |


### 6.2.7 ThongKePage: Thống kê và cảnh báo
Dùng cho chuyên gia và Quản trị viên. Chuyên gia chỉ chọn được khu vực mình phụ trách.

*(Sơ đồ trong báo cáo: Hình 7. Giao diện Thống kê và cảnh báo)*
| Thành phần | Kiểu | Chức năng |
|---|---|---|
| tuNgay, denNgay | Ô chọn ngày | Khoảng thời gian; ngày bắt đầu không sau ngày kết thúc. |
| locKhuVuc, locLoai | Danh sách chọn | Bộ lọc. |
| bangTongHop | Bảng | Số báo cáo theo trạng thái. |
| bieuDoTheoTuan | Biểu đồ cột (recharts) | Số ca đã xác minh theo tuần. |
| bangCanhBao | Bảng | Cảnh báo và dự báo trong kỳ, có cột nguồn BR06 hoặc AI. |


### 6.2.8 KhuVucPage: Khu vực và phân công
Quản trị viên quản lý khu vực và giao chuyên gia (UC14). DanhMucSauBenhPage và NguoiDungPage có bố cục giống trang này: bảng bên trái, ô nhập và nút bên phải.

*(Sơ đồ trong báo cáo: Hình 8. Giao diện Khu vực và phân công)*
| Thành phần | Kiểu | Chức năng |
|---|---|---|
| bangKhuVuc | Bảng | Danh sách khu vực, số chuyên gia phụ trách. |
| tenKhuVuc, moTa | Ô nhập văn bản | Thêm, sửa khu vực. |
| bangChuyenGia | Bảng | Chuyên gia của khu vực đang chọn. |
| chonChuyenGia | Danh sách chọn | Chuyên gia chưa bị khóa. |
| nutPhanCong, nutGoPhanCong | Nút | Thêm hoặc xóa dòng PhanCongKhuVuc. |


### 6.2.9 CauHinhPage: Cấu hình hệ thống
Sửa năm tham số trong ThamSoHeThong (UC16).

*(Sơ đồ trong báo cáo: Hình 9. Giao diện Cấu hình hệ thống)*
| Thành phần | Kiểu | Chức năng |
|---|---|---|
| bangThamSo | Bảng | Cột Giá trị cho sửa, các cột khác chỉ đọc. |
| nutLuu | Nút | Gọi `PUT /api/tham-so/{ma}` cho từng tham số đã sửa. |


### 6.2.10 MoHinhAIPage: Mô hình AI
Quản trị viên xem phiên bản đang dùng, chạy huấn luyện lại và kích hoạt phiên bản mới (UC19). Trong lúc huấn luyện, nút bị khóa và hiện vòng quay chờ; trang vẫn dùng được các phần khác.

*(Sơ đồ trong báo cáo: Hình 10. Giao diện Mô hình AI)*
| Thành phần | Kiểu | Chức năng |
|---|---|---|
| phienBanDangDung | Khung thông tin | Thông tin bản đang dùng. |
| nutHuanLuyen | Nút | Gọi `POST /api/mo-hinh-ai/huan-luyen`. |
| tienTrinh | Nhãn | Trạng thái huấn luyện. |
| bangPhienBan | Bảng | Các phiên bản với Accuracy, F1, AUC. |
| nutKichHoat | Nút | Gọi `POST /api/mo-hinh-ai/{ma}/kich-hoat` cho bản đang chọn. |

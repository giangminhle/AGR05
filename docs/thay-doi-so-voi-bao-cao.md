# Những chỗ code khác hoặc thêm so với báo cáo Word

File này liệt kê mọi chỗ cần sửa trong báo cáo Word để khớp với code. Phần đổi công nghệ (C# WinForms sang Spring Boot + React) xem bảng ở mục 6 của `README.md`.

## 1. Chức năng thêm mới

| Chức năng | Ai dùng | API | Service | Trang | Gợi ý sửa Word |
|---|---|---|---|---|---|
| Nông dân tự đăng ký | Khách (chưa đăng nhập) | `POST /api/auth/dang-ky` | `AccountService.dangKy(nd, matKhau)`: vai trò luôn là NONG_DAN | `DangKyPage` (`/dang-ky`), link ở trang đăng nhập | Thêm UC "Đăng ký tài khoản" (tác nhân Nông dân), đặc tả, sơ đồ tuần tự. Sửa `spec.md` dòng 12 cho khớp. Chuyên gia, Quản trị viên vẫn do Quản trị viên tạo (UC15). |
| Đổi mật khẩu | Mọi người dùng | `POST /api/auth/doi-mat-khau` | `AccountService.doiMatKhau(ma, matKhauCu, matKhauMoi)` | `DoiMatKhauPage` (`/doi-mat-khau`), trong menu tên người dùng | Thêm UC "Đổi mật khẩu" (include trong quản lý tài khoản cá nhân) hoặc ghi vào FR-01. |
| Đặt lại mật khẩu | Quản trị viên | `POST /api/nguoi-dung/{ma}/dat-lai-mat-khau` | `AccountService.datLaiMatKhau(ma, matKhauMoi)` | Nút "Đặt lại mật khẩu" trên `NguoiDungPage` | Bổ sung vào UC15 (luồng thay thế "người dùng quên mật khẩu"). |
| Tra cứu báo cáo | Quản trị viên | `GET /api/quan-tri/bao-cao`, `GET /api/quan-tri/bao-cao/{ma}` | `BaoCaoService.timKiemQuanTri`, `layChiTietQuanTri` | `QuanLyBaoCaoPage` (`/quan-tri/bao-cao`) | Lý do: thông báo "Báo cáo quá hạn" (BR02) gửi cho Quản trị viên nhưng thiết kế cũ không có màn hình để họ xem báo cáo đó. Thêm vào UC14 hoặc tạo UC "Tra cứu báo cáo". |
| Báo cáo đã xác minh chờ xử lý | Chuyên gia | `GET /api/xac-minh/da-xac-minh` | `XacMinhService.layDaXacMinh(maCG)` | Tab "Đã xác minh (chờ xử lý)" trên `XacMinhPage` | UC10 cần danh sách này để chọn báo cáo đánh dấu đã xử lý. |
| Xóa loại sâu bệnh | Quản trị viên | `DELETE /api/loai-sau-benh/{ma}` | `DanhMucService.xoaLoaiSauBenh` | Nút Xóa trên `DanhMucSauBenhPage` | Đã có trong UC13 (A1) nhưng thiếu trong bảng API, biểu đồ lớp. |
| Xem chuyên gia của khu vực | Quản trị viên | `GET /api/khu-vuc/{ma}/chuyen-gia` | `DanhMucService.layChuyenGiaCuaKhuVuc` | Bảng chuyên gia trên `KhuVucPage` | Bổ sung vào bảng API. |
| Sửa người dùng, đổi vai trò | Quản trị viên | `PUT /api/nguoi-dung/{ma}` | `AccountService.capNhat(nd)` | Form bên phải `NguoiDungPage` | Bổ sung vào bảng API, biểu đồ lớp. |

## 2. Tiện ích giao diện (không đổi nghiệp vụ, ghi vào Chương VI)

- Logo và chữ AGR-05 bấm vào thì về trang chính theo vai trò. Menu tên người dùng ở góc phải: Trang chính, Thông báo, Đổi mật khẩu, Đăng xuất.
- Mỗi trang có tiêu đề, mô tả ngắn và nút "Quay lại".
- Hộp xác nhận trước khi xóa, khóa, gỡ phân công, kích hoạt mô hình, đăng xuất.
- Phân trang và ô tìm kiếm cho các danh sách dài (báo cáo, thông báo, người dùng, danh mục).
- Nút "Lấy vị trí hiện tại" (GPS của trình duyệt) khi gửi báo cáo; link "Xem trên bản đồ" (Google Maps) ở chi tiết báo cáo.
- Trang Thông báo có nút mở báo cáo gắn với thông báo cho cả ba vai trò.

## 3. Chỗ đổi tham số hoặc làm rõ thiết kế

1. `XacMinhService.layChiTiet(ma, maCG)`: thêm `maCG` để kiểm tra khu vực phụ trách (BR05).
2. `ThongBaoService.taoThongBao(maNhan, loai, maBaoCao, tieuDe, noiDung)`: thêm mã báo cáo, tiêu đề, nội dung (bảng ThongBao có các cột này).
3. `HinhAnhDal.delete(maHinhAnh)`: để bỏ ảnh khi nông dân sửa, gửi lại báo cáo.
4. Mức nguy cơ: Cao khi xác suất ≥ NGUONG_NGUY_CO; Trung bình khi ≥ nửa ngưỡng; còn lại Thấp.
5. Chống cảnh báo trùng: cùng khu vực, cùng loại đã có cảnh báo (do BR06 hoặc AI) trong SO_NGAY_XET_NGUONG ngày thì không tạo cảnh báo mới.
6. Ngày xác minh của báo cáo lấy từ dòng `LichSuXacMinh` chuyển sang Đã xác minh (dùng cho BR06 và đặc trưng AI).
7. Precision, Recall của mô hình chỉ ghi nhật ký, không lưu vì bảng MoHinhAI không có cột.
8. Đánh dấu đã xử lý (UC10) gửi thông báo loại KET_QUA_XAC_MINH cho nông dân; phân công khu vực không tạo thông báo.
9. Mật khẩu: 6 đến 100 ký tự.

10. **Đề nghị thêm loại sâu bệnh** (mới): khi loại trên ảnh chưa có trong danh mục, chuyên gia bấm "Đề nghị thêm loại mới" ở form xác minh (`POST /api/xac-minh/{ma}/de-nghi-loai`, `XacMinhService.deNghiThemLoai`). Báo cáo giữ Chờ xác minh, lịch sử ghi một dòng Chờ xác minh → Chờ xác minh kèm tên loại đề nghị, mọi Quản trị viên nhận thông báo. Quản trị viên thêm loại ở Danh mục sâu bệnh (UC13) thì mọi chuyên gia đang hoạt động nhận thông báo "Danh mục có loại sâu bệnh mới". Không có loại "Khác" vì BR06 và AI đếm ca theo từng loại. Cần bổ sung luồng phụ này vào đặc tả UC07 và UC13.
11. **Tài khoản bị khóa hoặc đổi vai trò** phải đăng nhập lại ngay ở lần gọi API kế tiếp (`KiemTraPhienFilter`), không đợi hết phiên 8 giờ.

CSDL: thêm một dòng danh mục `LoaiThongBao` mã `DANH_MUC` (Danh mục sâu bệnh); không đổi bảng, cột. CSDL đã tạo trước đó chạy `db/nang_cap_loai_thong_bao.sql`.

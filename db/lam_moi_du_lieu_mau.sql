/* Làm mới dữ liệu mẫu: xóa tài khoản mẫu (cả bản cũ *_demo) cùng mọi báo cáo họ gửi, xóa toàn bộ thông báo, cảnh báo,
   dự báo, phiên bản mô hình AI, phân công và loại sâu bệnh thêm khi demo; đổi tên khu vực cũ sang tên mới. GIỮ LẠI tài khoản và báo cáo
   do người dùng thật tạo (không nằm trong danh sách dưới).
   Đây là script quản trị dùng khi chuẩn bị demo; ứng dụng không có chức năng xóa báo cáo (RB06).
   Chạy:  sqlcmd -S . -E -I -f 65001 -i db\lam_moi_du_lieu_mau.sql
   Sau đó chạy ứng dụng với profile demo một lần để tạo lại dữ liệu mẫu (xem docs/kich-ban-demo.md). */
USE AGR05_QuanLySauBenh;
SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRANSACTION;

DECLARE @nd TABLE (MaNguoiDung INT PRIMARY KEY);
INSERT INTO @nd SELECT MaNguoiDung FROM NguoiDung WHERE TenDangNhap IN (
    'qt.minhkhoa', 'cg.thanhtam', 'cg.ngocanh', 'nd.vanbay', 'nd.thimai', 'nd.vantu', 'nd.vanut',
    'nongdan_demo', 'nongdan_demo2', 'chuyengia_demo', 'chuyengia_demo2', 'quantri_demo', 'chuyengia_thu', 'nongdan_moi');

DECLARE @bc TABLE (MaBaoCao INT PRIMARY KEY);
INSERT INTO @bc SELECT MaBaoCao FROM BaoCaoHienTruong WHERE MaNguoiGui IN (SELECT MaNguoiDung FROM @nd);

DELETE FROM CanhBao;
DELETE FROM DuBaoNguyCo;
DELETE FROM MoHinhAI;
DELETE FROM ThongBao;
DELETE FROM PhanCongKhuVuc;
DELETE FROM LichSuXacMinh WHERE MaBaoCao IN (SELECT MaBaoCao FROM @bc);
DELETE FROM HinhAnh WHERE MaBaoCao IN (SELECT MaBaoCao FROM @bc);
DELETE FROM BaoCaoHienTruong WHERE MaBaoCao IN (SELECT MaBaoCao FROM @bc);
-- Báo cáo còn lại (của người dùng thật) có thể đã được tài khoản mẫu duyệt: giữ lịch sử, bỏ liên kết người thực hiện
UPDATE LichSuXacMinh SET MaNguoiThucHien = NULL WHERE MaNguoiThucHien IN (SELECT MaNguoiDung FROM @nd);
DELETE FROM NguoiDung WHERE MaNguoiDung IN (SELECT MaNguoiDung FROM @nd);

-- Loại sâu bệnh thêm trong lúc demo (ngoài 5 loại của db/seed_demo.sql) mà không còn báo cáo, cảnh báo nào dùng
DELETE FROM LoaiSauBenh
WHERE TenLoai NOT IN (N'Rầy nâu', N'Bệnh đạo ôn', N'Sâu cuốn lá nhỏ', N'Bệnh bạc lá', N'Sâu đục thân')
  AND MaLoai NOT IN (SELECT MaLoai FROM BaoCaoHienTruong WHERE MaLoai IS NOT NULL)
  AND MaLoai NOT IN (SELECT MaLoai FROM CanhBao);

-- Đổi tên khu vực cũ (giữ mã để báo cáo còn lại không mất khu vực)
UPDATE KhuVuc SET TenKhuVuc = N'Xã Định Môn - Cánh đồng mẫu lớn', MoTa = N'Huyện Thới Lai, TP Cần Thơ. Liên kết tiêu thụ, khoảng 200 ha'
    WHERE TenKhuVuc = N'Xã A - Cánh đồng 1';
UPDATE KhuVuc SET TenKhuVuc = N'Xã Trường Xuân - Vùng lúa chất lượng cao', MoTa = N'Huyện Thới Lai, TP Cần Thơ. Giống ST25, OM18, khoảng 120 ha'
    WHERE TenKhuVuc = N'Xã A - Cánh đồng 2';
UPDATE KhuVuc SET TenKhuVuc = N'Xã Xuân Thắng - Ruộng ven kênh', MoTa = N'Huyện Thới Lai, TP Cần Thơ. Ruộng nhỏ ven kênh, khoảng 80 ha'
    WHERE TenKhuVuc = N'Xã B - Vùng trồng rau';
UPDATE KhuVuc SET TenKhuVuc = N'Xã Thới Thạnh - Cánh đồng lớn', MoTa = N'Huyện Thới Lai, TP Cần Thơ. Lúa 3 vụ, khoảng 150 ha'
    WHERE TenKhuVuc = N'Xã C - Ruộng trình diễn';

COMMIT;

PRINT N'Đã xóa dữ liệu mẫu. Chạy ứng dụng với profile demo một lần để tạo lại.';
PRINT N'File ảnh cũ trong thư mục ảnh và file mô hình cũ không còn được dùng, có thể xóa tay.';

/* Ôn tập truy vấn AGR-05. Chạy trên CSDL AGR05_QuanLySauBenh.
   Các câu UPDATE/INSERT/DELETE/CREATE nằm trong khối chú thích để tránh sửa dữ liệu demo;
   muốn chạy thì bỏ /* */ và nên chạy trong BEGIN TRAN ... ROLLBACK. */
USE AGR05_QuanLySauBenh;
GO


-- ============================================================
-- A. Lọc cơ bản (SELECT, WHERE, ORDER BY)
-- ============================================================

-- Câu 1. Liệt kê các báo cáo đang chờ xác minh, báo cáo gửi sớm nhất lên trước.
SELECT MaBaoCao, TieuDe, NgayGui
FROM BaoCaoHienTruong
WHERE MaTrangThai = 'CHO_XAC_MINH'
ORDER BY NgayGui ASC;
GO

-- Câu 2. Tìm các báo cáo có tiêu đề chứa chữ "rầy".
SELECT MaBaoCao, TieuDe
FROM BaoCaoHienTruong
WHERE TieuDe LIKE N'%rầy%';
GO

-- Câu 3. Liệt kê báo cáo gửi trong tháng 11/2026 có mức độ nặng (MucDo = 3).
SELECT MaBaoCao, TieuDe, MucDo, NgayGui
FROM BaoCaoHienTruong
WHERE NgayGui >= '2026-11-01' AND NgayGui < '2026-12-01'
  AND MucDo = 3;
GO

-- Câu 4. Liệt kê các tài khoản đang bị khóa và vai trò của họ.
SELECT n.TenDangNhap, n.HoTen, v.TenVaiTro
FROM NguoiDung n
JOIN VaiTro v ON v.MaVaiTro = n.MaVaiTro
WHERE n.BiKhoa = 1;
GO

-- ============================================================
-- B. Kết bảng (JOIN)
-- ============================================================

-- Câu 5. Danh sách báo cáo kèm tên người gửi, tên khu vực, tên trạng thái và tên loại sâu bệnh.
SELECT b.MaBaoCao, b.TieuDe, n.HoTen AS NguoiGui, k.TenKhuVuc,
       t.TenTrangThai, l.TenLoai
FROM BaoCaoHienTruong b
JOIN NguoiDung n       ON n.MaNguoiDung = b.MaNguoiGui
JOIN KhuVuc k          ON k.MaKhuVuc = b.MaKhuVuc
JOIN TrangThaiBaoCao t ON t.MaTrangThai = b.MaTrangThai
LEFT JOIN LoaiSauBenh l ON l.MaLoai = b.MaLoai;
GO

-- Câu 6. Mỗi chuyên gia phụ trách những khu vực nào? Gộp tên khu vực thành một dòng.
SELECT n.HoTen,
       STRING_AGG(k.TenKhuVuc, N', ') WITHIN GROUP (ORDER BY k.TenKhuVuc) AS KhuVucPhuTrach
FROM NguoiDung n
JOIN PhanCongKhuVuc p ON p.MaNguoiDung = n.MaNguoiDung
JOIN KhuVuc k         ON k.MaKhuVuc = p.MaKhuVuc
WHERE n.MaVaiTro = 'CHUYEN_GIA'
GROUP BY n.HoTen;
GO

-- Câu 7. Danh sách chờ xác minh của chuyên gia có mã @MaCG (đúng như màn hình XacMinhPage).
DECLARE @MaCG INT = 2;
SELECT b.MaBaoCao, b.TieuDe, b.NgayGui, b.QuaHan
FROM BaoCaoHienTruong b
WHERE b.MaTrangThai = 'CHO_XAC_MINH'
  AND b.MaKhuVuc IN (SELECT MaKhuVuc FROM PhanCongKhuVuc WHERE MaNguoiDung = @MaCG)
  AND b.MaNguoiGui <> @MaCG          -- BR05: không tự duyệt báo cáo của mình
ORDER BY b.NgayGui;
GO

-- Câu 8. Xem lịch sử xử lý của báo cáo số 8, hiện "Hệ thống" khi không có người thực hiện.
SELECT x.ThoiGian, ISNULL(n.HoTen, N'Hệ thống') AS NguoiThucHien,
       x.MaTrangThaiCu, x.MaTrangThaiMoi, x.GhiChu
FROM LichSuXacMinh x
LEFT JOIN NguoiDung n ON n.MaNguoiDung = x.MaNguoiThucHien
WHERE x.MaBaoCao = 8
ORDER BY x.ThoiGian;
GO

-- ============================================================
-- C. Gom nhóm (GROUP BY, HAVING)
-- ============================================================

-- Câu 9. Đếm số báo cáo theo từng trạng thái, kể cả trạng thái chưa có báo cáo nào.
SELECT t.TenTrangThai, COUNT(b.MaBaoCao) AS SoLuong
FROM TrangThaiBaoCao t
LEFT JOIN BaoCaoHienTruong b ON b.MaTrangThai = t.MaTrangThai
GROUP BY t.TenTrangThai, t.ThuTu
ORDER BY t.ThuTu;
GO

-- Câu 10. Những nông dân đã gửi từ 3 báo cáo trở lên.
SELECT n.HoTen, COUNT(*) AS SoBaoCao
FROM NguoiDung n
JOIN BaoCaoHienTruong b ON b.MaNguoiGui = n.MaNguoiDung
WHERE n.MaVaiTro = 'NONG_DAN'
GROUP BY n.HoTen
HAVING COUNT(*) >= 3;
GO

-- Câu 11. Quy tắc BR06: khu vực và loại sâu bệnh nào đã đủ ngưỡng cảnh báo (đọc ngưỡng và số ngày từ bảng tham số)?
DECLARE @Nguong INT = (SELECT GiaTri FROM ThamSoHeThong WHERE MaThamSo = 'NGUONG_CANH_BAO');
DECLARE @SoNgay INT = (SELECT GiaTri FROM ThamSoHeThong WHERE MaThamSo = 'SO_NGAY_XET_NGUONG');

SELECT k.TenKhuVuc, l.TenLoai, COUNT(*) AS SoCa
FROM BaoCaoHienTruong b
JOIN LichSuXacMinh x ON x.MaBaoCao = b.MaBaoCao AND x.MaTrangThaiMoi = 'DA_XAC_MINH'
JOIN KhuVuc k        ON k.MaKhuVuc = b.MaKhuVuc
JOIN LoaiSauBenh l   ON l.MaLoai = b.MaLoai
WHERE b.MaTrangThai IN ('DA_XAC_MINH', 'DA_XU_LY')
  AND x.ThoiGian >= DATEADD(DAY, -@SoNgay, SYSDATETIME())
GROUP BY k.TenKhuVuc, l.TenLoai
HAVING COUNT(*) >= @Nguong;
GO

-- Câu 12. Thống kê số ca đã xác minh theo tuần (năm, số tuần ISO).
SELECT YEAR(x.ThoiGian) AS Nam, DATEPART(ISO_WEEK, x.ThoiGian) AS Tuan, COUNT(*) AS SoCa
FROM LichSuXacMinh x
JOIN BaoCaoHienTruong b ON b.MaBaoCao = x.MaBaoCao
WHERE x.MaTrangThaiMoi = 'DA_XAC_MINH'
  AND b.MaTrangThai IN ('DA_XAC_MINH', 'DA_XU_LY')
GROUP BY YEAR(x.ThoiGian), DATEPART(ISO_WEEK, x.ThoiGian)
ORDER BY Nam, Tuan;
GO

-- Câu 13. Chuyên gia nào duyệt nhiều báo cáo nhất (nếu bằng nhau thì lấy hết)?
SELECT TOP 1 WITH TIES n.HoTen, COUNT(*) AS SoLanDuyet
FROM LichSuXacMinh x
JOIN NguoiDung n ON n.MaNguoiDung = x.MaNguoiThucHien
WHERE x.MaTrangThaiMoi = 'DA_XAC_MINH'
GROUP BY n.HoTen
ORDER BY SoLanDuyet DESC;
GO

-- Câu 14. Với mỗi chuyên gia, đếm số lần duyệt và số lần bác bỏ trên cùng một dòng (chuyên gia chưa làm gì vẫn hiện 0).
SELECT n.HoTen,
       SUM(CASE WHEN x.MaTrangThaiMoi = 'DA_XAC_MINH' THEN 1 ELSE 0 END) AS SoDuyet,
       SUM(CASE WHEN x.MaTrangThaiMoi = 'BI_BAC_BO'   THEN 1 ELSE 0 END) AS SoBacBo
FROM NguoiDung n
LEFT JOIN LichSuXacMinh x ON x.MaNguoiThucHien = n.MaNguoiDung
                         AND x.MaTrangThaiMoi IN ('DA_XAC_MINH', 'BI_BAC_BO')
WHERE n.MaVaiTro = 'CHUYEN_GIA'
GROUP BY n.HoTen;
GO

-- ============================================================
-- D. Truy vấn con, NOT EXISTS, kiểm tra dữ liệu
-- ============================================================

-- Câu 15. Tìm báo cáo không có ảnh nào (kiểm tra vi phạm BR01).
SELECT b.MaBaoCao, b.TieuDe
FROM BaoCaoHienTruong b
WHERE NOT EXISTS (SELECT 1 FROM HinhAnh h WHERE h.MaBaoCao = b.MaBaoCao);
GO

-- Câu 16. Khu vực nào chưa có chuyên gia phụ trách?
SELECT k.TenKhuVuc
FROM KhuVuc k
WHERE NOT EXISTS (SELECT 1 FROM PhanCongKhuVuc p WHERE p.MaKhuVuc = k.MaKhuVuc);
GO

-- Câu 17. Loại sâu bệnh nào chưa từng được xác nhận trong báo cáo nào?
SELECT l.TenLoai
FROM LoaiSauBenh l
WHERE l.MaLoai NOT IN (SELECT b.MaLoai FROM BaoCaoHienTruong b
                       WHERE b.MaLoai IS NOT NULL);
GO

-- Câu 18. Báo cáo nào đang quá hạn xác minh (chờ quá HAN_XAC_MINH_GIO giờ, BR02)?
DECLARE @Han INT = (SELECT GiaTri FROM ThamSoHeThong WHERE MaThamSo = 'HAN_XAC_MINH_GIO');
SELECT b.MaBaoCao, b.TieuDe, b.NgayVaoTrangThai,
       DATEDIFF(HOUR, b.NgayVaoTrangThai, SYSDATETIME()) AS SoGioCho
FROM BaoCaoHienTruong b
WHERE b.MaTrangThai = 'CHO_XAC_MINH'
  AND b.NgayVaoTrangThai <= DATEADD(HOUR, -@Han, SYSDATETIME());
GO

-- Câu 19. Báo cáo bị bác bỏ nào vẫn còn được gửi lại (BR04: chưa gửi lại lần nào và còn trong hạn)?
DECLARE @HanGuiLai INT = (SELECT GiaTri FROM ThamSoHeThong WHERE MaThamSo = 'HAN_GUI_LAI_NGAY');
SELECT b.MaBaoCao, b.TieuDe, b.NgayVaoTrangThai
FROM BaoCaoHienTruong b
WHERE b.MaTrangThai = 'BI_BAC_BO'
  AND b.SoLanGuiLai = 0
  AND b.NgayVaoTrangThai > DATEADD(DAY, -@HanGuiLai, SYSDATETIME());
GO

-- ============================================================
-- E. Mô-đun AI
-- ============================================================

-- Câu 20. Phiên bản mô hình nào đang được dùng, và các chỉ số của nó?
SELECT PhienBan, NgayHuanLuyen, SoMauHuanLuyen, Accuracy, F1, AUC
FROM MoHinhAI
WHERE DangSuDung = 1;
GO

-- Câu 21. Liệt kê các dự báo mức CAO kèm khu vực, loại sâu bệnh, và cảnh báo đã sinh ra từ dự báo đó (nếu có).
SELECT d.MaDuBao, k.TenKhuVuc, l.TenLoai, d.XacSuat, m.PhienBan, c.MaCanhBao
FROM DuBaoNguyCo d
JOIN BaoCaoHienTruong b ON b.MaBaoCao = d.MaBaoCao
JOIN KhuVuc k           ON k.MaKhuVuc = b.MaKhuVuc
JOIN LoaiSauBenh l      ON l.MaLoai = b.MaLoai
JOIN MoHinhAI m         ON m.MaMoHinh = d.MaMoHinh
LEFT JOIN CanhBao c     ON c.MaDuBao = d.MaDuBao
WHERE d.MucNguyCo = 'CAO'
ORDER BY d.XacSuat DESC;
GO

-- Câu 22. Đếm cảnh báo theo nguồn: theo ngưỡng BR06 hay theo AI (BR08).
SELECT CASE WHEN MaDuBao IS NULL THEN N'Ngưỡng BR06' ELSE N'Dự báo AI BR08' END AS Nguon,
       COUNT(*) AS SoCanhBao
FROM CanhBao
GROUP BY CASE WHEN MaDuBao IS NULL THEN N'Ngưỡng BR06' ELSE N'Dự báo AI BR08' END;
GO

-- ============================================================
-- F. Nâng cao (hàm cửa sổ, tính thời gian)
-- ============================================================

-- Câu 23. Với mỗi khu vực, loại sâu bệnh nào xuất hiện nhiều nhất (tính trên báo cáo đã xác minh)?
SELECT TenKhuVuc, TenLoai, SoCa
FROM (
    SELECT k.TenKhuVuc, l.TenLoai, COUNT(*) AS SoCa,
           RANK() OVER (PARTITION BY k.MaKhuVuc ORDER BY COUNT(*) DESC) AS Hang
    FROM BaoCaoHienTruong b
    JOIN KhuVuc k      ON k.MaKhuVuc = b.MaKhuVuc
    JOIN LoaiSauBenh l ON l.MaLoai = b.MaLoai
    WHERE b.MaTrangThai IN ('DA_XAC_MINH', 'DA_XU_LY')
    GROUP BY k.MaKhuVuc, k.TenKhuVuc, l.TenLoai
) t
WHERE Hang = 1;
GO

-- Câu 24. Thời gian xử lý trung bình (giờ) của mỗi chuyên gia, tính từ lúc gửi đến lúc duyệt hoặc bác bỏ.
SELECT n.HoTen,
       AVG(CAST(DATEDIFF(MINUTE, b.NgayGui, x.ThoiGian) AS FLOAT)) / 60 AS SoGioTrungBinh,
       COUNT(*) AS SoLanXuLy
FROM LichSuXacMinh x
JOIN BaoCaoHienTruong b ON b.MaBaoCao = x.MaBaoCao
JOIN NguoiDung n        ON n.MaNguoiDung = x.MaNguoiThucHien
WHERE x.MaTrangThaiCu = 'CHO_XAC_MINH'
  AND x.MaTrangThaiMoi IN ('DA_XAC_MINH', 'BI_BAC_BO')
GROUP BY n.HoTen;
GO

-- Câu 25. Viết bằng CTE: tỉ lệ báo cáo bị bác bỏ của từng khu vực (phần trăm).
WITH ThongKe AS (
    SELECT MaKhuVuc,
           COUNT(*) AS Tong,
           SUM(CASE WHEN MaTrangThai = 'BI_BAC_BO' THEN 1 ELSE 0 END) AS BacBo
    FROM BaoCaoHienTruong
    GROUP BY MaKhuVuc
)
SELECT k.TenKhuVuc, t.Tong, t.BacBo,
       CAST(100.0 * t.BacBo / t.Tong AS DECIMAL(5,2)) AS TiLePhanTram
FROM ThongKe t
JOIN KhuVuc k ON k.MaKhuVuc = t.MaKhuVuc;
GO

-- ============================================================
-- G. Thêm, sửa, xóa, transaction
-- ============================================================

-- Câu 26. Viết câu duyệt báo cáo @Ma sao cho hai chuyên gia bấm cùng lúc không duyệt trùng.
/*
DECLARE @Ma INT = 5, @MaLoai INT = 1, @MaCG INT = 3;
BEGIN TRY
    BEGIN TRAN;
    UPDATE BaoCaoHienTruong
    SET MaTrangThai = 'DA_XAC_MINH', MaLoai = @MaLoai, NgayVaoTrangThai = SYSDATETIME()
    WHERE MaBaoCao = @Ma AND MaTrangThai = 'CHO_XAC_MINH';   -- điều kiện trạng thái cũ

    IF @@ROWCOUNT = 0
        THROW 50001, N'Báo cáo đã được xử lý.', 1;

    INSERT INTO LichSuXacMinh (MaBaoCao, MaNguoiThucHien, MaTrangThaiCu, MaTrangThaiMoi)
    VALUES (@Ma, @MaCG, 'CHO_XAC_MINH', 'DA_XAC_MINH');
    COMMIT;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;
*/

-- Câu 27. Gắn cờ quá hạn hàng loạt cho các báo cáo chờ quá 48 giờ (việc chức năng nền FR-17 làm).
/*
UPDATE BaoCaoHienTruong
SET QuaHan = 1
WHERE MaTrangThai = 'CHO_XAC_MINH'
  AND QuaHan = 0
  AND NgayVaoTrangThai <= DATEADD(HOUR,
        -(SELECT GiaTri FROM ThamSoHeThong WHERE MaThamSo = 'HAN_XAC_MINH_GIO'), SYSDATETIME());
*/

-- Câu 28. Đánh dấu tất cả thông báo của người dùng @Ma là đã đọc; đếm số thông báo chưa đọc.
/*
DECLARE @Ma INT = 4;
SELECT COUNT(*) AS ChuaDoc FROM ThongBao WHERE MaNguoiNhan = @Ma AND DaDoc = 0;
UPDATE ThongBao SET DaDoc = 1 WHERE MaNguoiNhan = @Ma AND DaDoc = 0;
*/

-- Câu 29. Thêm một loại sâu bệnh mới và đổi ngưỡng cảnh báo thành 6.
/*
INSERT INTO LoaiSauBenh (TenLoai, TrieuChung, BienPhapPhongTru)
VALUES (N'Bọ trĩ', N'Lá non xoăn, có vết bạc', NULL);

UPDATE ThamSoHeThong SET GiaTri = 6 WHERE MaThamSo = 'NGUONG_CANH_BAO';
*/

-- Câu 30. Thầy yêu cầu xóa báo cáo số 6 thì sao?
/*
-- Hệ thống không cho xóa báo cáo (DAL không có hàm xóa) để giữ lịch sử.
-- Nếu buộc phải xóa trong SQL, phải xóa bảng con trước vì có khóa ngoại:
BEGIN TRAN;
DELETE FROM ThongBao      WHERE MaBaoCao = 6;
DELETE FROM LichSuXacMinh WHERE MaBaoCao = 6;
DELETE FROM CanhBao       WHERE MaDuBao IN (SELECT MaDuBao FROM DuBaoNguyCo WHERE MaBaoCao = 6);
DELETE FROM DuBaoNguyCo   WHERE MaBaoCao = 6;
DELETE FROM BaoCaoHienTruong WHERE MaBaoCao = 6;   -- HinhAnh tự xóa nhờ ON DELETE CASCADE
COMMIT;
*/

-- ============================================================
-- H. View và stored procedure
-- ============================================================

-- Câu 31. Tạo view báo cáo chi tiết để các màn hình dùng chung.
/*
CREATE VIEW vw_BaoCaoChiTiet AS
SELECT b.MaBaoCao, b.TieuDe, b.MucDo, b.NgayGui, b.QuaHan,
       n.HoTen AS NguoiGui, k.TenKhuVuc, t.TenTrangThai, l.TenLoai,
       (SELECT COUNT(*) FROM HinhAnh h WHERE h.MaBaoCao = b.MaBaoCao) AS SoAnh
FROM BaoCaoHienTruong b
JOIN NguoiDung n       ON n.MaNguoiDung = b.MaNguoiGui
JOIN KhuVuc k          ON k.MaKhuVuc = b.MaKhuVuc
JOIN TrangThaiBaoCao t ON t.MaTrangThai = b.MaTrangThai
LEFT JOIN LoaiSauBenh l ON l.MaLoai = b.MaLoai;
*/

-- Câu 32. Viết stored procedure thống kê số ca đã xác minh theo khu vực trong khoảng ngày @Tu đến @Den.
/*
CREATE PROCEDURE sp_ThongKeTheoKhuVuc @Tu DATE, @Den DATE
AS
BEGIN
    SET NOCOUNT ON;
    SELECT k.TenKhuVuc, COUNT(x.MaLichSu) AS SoCa
    FROM KhuVuc k
    LEFT JOIN BaoCaoHienTruong b ON b.MaKhuVuc = k.MaKhuVuc
                                AND b.MaTrangThai IN ('DA_XAC_MINH', 'DA_XU_LY')
    LEFT JOIN LichSuXacMinh x    ON x.MaBaoCao = b.MaBaoCao
                                AND x.MaTrangThaiMoi = 'DA_XAC_MINH'
                                AND x.ThoiGian >= @Tu
                                AND x.ThoiGian < DATEADD(DAY, 1, @Den)
    GROUP BY k.TenKhuVuc
    ORDER BY SoCa DESC;
END;
-- Gọi: EXEC sp_ThongKeTheoKhuVuc '2026-11-01', '2026-11-30';
*/

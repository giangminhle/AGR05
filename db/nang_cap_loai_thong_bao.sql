/* Nâng cấp CSDL đã tạo trước khi có chức năng "Đề nghị thêm loại sâu bệnh": thêm loại thông báo DANH_MUC.
   Chạy được nhiều lần.  sqlcmd -S . -E -I -f 65001 -i db\nang_cap_loai_thong_bao.sql */
USE AGR05_QuanLySauBenh;
IF NOT EXISTS (SELECT 1 FROM LoaiThongBao WHERE MaLoaiTB = 'DANH_MUC')
    INSERT INTO LoaiThongBao (MaLoaiTB, TenLoaiTB) VALUES ('DANH_MUC', N'Danh mục sâu bệnh');
PRINT N'Đã có loại thông báo DANH_MUC.';

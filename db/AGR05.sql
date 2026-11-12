/* =====================================================
   AGR-05: Quản lý cảnh báo sâu bệnh dựa trên báo cáo hiện trường có xác minh
   Script T-SQL cho SQL Server. 15 bảng, 87 cột.
   Tài khoản người dùng KHÔNG tạo ở đây: mật khẩu phải băm PBKDF2 trong C#,
   nên tài khoản quản trị đầu tiên do DbSeeder tạo khi chạy ứng dụng lần đầu.
   ===================================================== */
-- 1. TẠO CSDL --------------------------------------------------
IF DB_ID(N'AGR05_QuanLySauBenh') IS NULL
    CREATE DATABASE AGR05_QuanLySauBenh COLLATE Vietnamese_CI_AS;
GO

USE AGR05_QuanLySauBenh;
GO

-- 2. BẢNG DANH MỤC (quy tắc 8) ---------------------------------
CREATE TABLE VaiTro (
    MaVaiTro   VARCHAR(20)   NOT NULL,
    TenVaiTro  NVARCHAR(50)  NOT NULL,
    CONSTRAINT PK_VaiTro PRIMARY KEY (MaVaiTro)
);

CREATE TABLE TrangThaiBaoCao (
    MaTrangThai   VARCHAR(20)   NOT NULL,
    TenTrangThai  NVARCHAR(50)  NOT NULL,
    ThuTu         TINYINT       NOT NULL,
    CONSTRAINT PK_TrangThaiBaoCao PRIMARY KEY (MaTrangThai),
    CONSTRAINT UQ_TrangThaiBaoCao_ThuTu UNIQUE (ThuTu)
);

CREATE TABLE LoaiThongBao (
    MaLoaiTB   VARCHAR(20)   NOT NULL,
    TenLoaiTB  NVARCHAR(50)  NOT NULL,
    CONSTRAINT PK_LoaiThongBao PRIMARY KEY (MaLoaiTB)
);

-- 3. BẢNG THAM SỐ HỆ THỐNG (quy tắc 9) --------------------------
CREATE TABLE ThamSoHeThong (
    MaThamSo  VARCHAR(50)   NOT NULL,
    GiaTri    INT           NOT NULL,
    MoTa      NVARCHAR(255) NULL,
    CONSTRAINT PK_ThamSoHeThong PRIMARY KEY (MaThamSo),
    CONSTRAINT CK_ThamSoHeThong_GiaTri CHECK (GiaTri > 0),
    -- Ngưỡng nguy cơ là phần trăm (BR08)
    CONSTRAINT CK_ThamSoHeThong_NguongNguyCo CHECK (MaThamSo <> 'NGUONG_NGUY_CO' OR GiaTri BETWEEN 1 AND 100)
);

-- 4. NGƯỜI DÙNG, KHU VỰC, PHÂN CÔNG ----------------------------
CREATE TABLE NguoiDung (
    MaNguoiDung  INT IDENTITY(1,1) NOT NULL,
    TenDangNhap  VARCHAR(50)       NOT NULL,
    MatKhauHash  VARCHAR(100)      NOT NULL,
    Muoi         VARCHAR(48)       NOT NULL,
    HoTen        NVARCHAR(100)     NOT NULL,
    DienThoai    VARCHAR(15)       NULL,
    MaVaiTro     VARCHAR(20)       NOT NULL,
    BiKhoa       BIT               NOT NULL CONSTRAINT DF_NguoiDung_BiKhoa DEFAULT 0,
    NgayTao      DATETIME2(0)      NOT NULL CONSTRAINT DF_NguoiDung_NgayTao DEFAULT SYSDATETIME(),
    CONSTRAINT PK_NguoiDung PRIMARY KEY (MaNguoiDung),
    CONSTRAINT UQ_NguoiDung_TenDangNhap UNIQUE (TenDangNhap),
    CONSTRAINT FK_NguoiDung_VaiTro FOREIGN KEY (MaVaiTro) REFERENCES VaiTro (MaVaiTro),
    CONSTRAINT CK_NguoiDung_DienThoai CHECK (DienThoai IS NULL OR DienThoai NOT LIKE '%[^0-9]%')
);

CREATE TABLE KhuVuc (
    MaKhuVuc   INT IDENTITY(1,1) NOT NULL,
    TenKhuVuc  NVARCHAR(100)     NOT NULL,
    MoTa       NVARCHAR(255)     NULL,
    CONSTRAINT PK_KhuVuc PRIMARY KEY (MaKhuVuc),
    CONSTRAINT UQ_KhuVuc_TenKhuVuc UNIQUE (TenKhuVuc)
);

-- Bảng trung gian n-n (quy tắc 4)
CREATE TABLE PhanCongKhuVuc (
    MaNguoiDung   INT          NOT NULL,
    MaKhuVuc      INT          NOT NULL,
    NgayPhanCong  DATETIME2(0) NOT NULL CONSTRAINT DF_PhanCong_Ngay DEFAULT SYSDATETIME(),
    CONSTRAINT PK_PhanCongKhuVuc PRIMARY KEY (MaNguoiDung, MaKhuVuc),
    CONSTRAINT FK_PhanCong_NguoiDung FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung (MaNguoiDung),
    CONSTRAINT FK_PhanCong_KhuVuc    FOREIGN KEY (MaKhuVuc)    REFERENCES KhuVuc (MaKhuVuc)
);

-- 5. DANH MỤC SÂU BỆNH -----------------------------------------
CREATE TABLE LoaiSauBenh (
    MaLoai            INT IDENTITY(1,1) NOT NULL,
    TenLoai           NVARCHAR(100)     NOT NULL,
    TrieuChung        NVARCHAR(1000)    NULL,
    BienPhapPhongTru  NVARCHAR(1000)    NULL,
    CONSTRAINT PK_LoaiSauBenh PRIMARY KEY (MaLoai),
    CONSTRAINT UQ_LoaiSauBenh_TenLoai UNIQUE (TenLoai)
);

-- 6. BÁO CÁO HIỆN TRƯỜNG ---------------------------------------
CREATE TABLE BaoCaoHienTruong (
    MaBaoCao          INT IDENTITY(1,1) NOT NULL,
    TieuDe            NVARCHAR(200)     NOT NULL,
    TenCayTrong       NVARCHAR(100)     NOT NULL,
    MoTa              NVARCHAR(1000)    NOT NULL,
    MucDo             TINYINT           NOT NULL,
    ViDo              DECIMAL(9,6)      NOT NULL,
    KinhDo            DECIMAL(9,6)      NOT NULL,
    MaTrangThai       VARCHAR(20)       NOT NULL CONSTRAINT DF_BaoCao_TrangThai DEFAULT 'CHO_XAC_MINH',
    KhuyenNghi        NVARCHAR(1000)    NULL,
    NgayPhatHien      DATE              NOT NULL,
    NgayGui           DATETIME2(0)      NOT NULL CONSTRAINT DF_BaoCao_NgayGui DEFAULT SYSDATETIME(),
    NgayVaoTrangThai  DATETIME2(0)      NOT NULL CONSTRAINT DF_BaoCao_NgayVao DEFAULT SYSDATETIME(),
    SoLanGuiLai       TINYINT           NOT NULL CONSTRAINT DF_BaoCao_SoLanGuiLai DEFAULT 0,
    QuaHan            BIT               NOT NULL CONSTRAINT DF_BaoCao_QuaHan DEFAULT 0,
    MaNguoiGui        INT               NOT NULL,
    MaKhuVuc          INT               NOT NULL,
    MaLoai            INT               NULL,
    CONSTRAINT PK_BaoCaoHienTruong PRIMARY KEY (MaBaoCao),
    CONSTRAINT FK_BaoCao_TrangThai FOREIGN KEY (MaTrangThai) REFERENCES TrangThaiBaoCao (MaTrangThai),
    CONSTRAINT FK_BaoCao_NguoiGui  FOREIGN KEY (MaNguoiGui)  REFERENCES NguoiDung (MaNguoiDung),
    CONSTRAINT FK_BaoCao_KhuVuc    FOREIGN KEY (MaKhuVuc)    REFERENCES KhuVuc (MaKhuVuc),
    CONSTRAINT FK_BaoCao_Loai      FOREIGN KEY (MaLoai)      REFERENCES LoaiSauBenh (MaLoai),
    CONSTRAINT CK_BaoCao_MucDo       CHECK (MucDo BETWEEN 1 AND 3),
    CONSTRAINT CK_BaoCao_ViDo        CHECK (ViDo BETWEEN -90 AND 90),
    CONSTRAINT CK_BaoCao_KinhDo      CHECK (KinhDo BETWEEN -180 AND 180),
    CONSTRAINT CK_BaoCao_SoLanGuiLai CHECK (SoLanGuiLai IN (0, 1)),
    CONSTRAINT CK_BaoCao_NgayPhatHien CHECK (NgayPhatHien <= CAST(NgayGui AS DATE)),
    CONSTRAINT CK_BaoCao_DaXacMinh_CoLoai CHECK (MaTrangThai <> 'DA_XAC_MINH' OR MaLoai IS NOT NULL)
);

-- 7. HÌNH ẢNH (quy tắc 7: thuộc tính mảng) ---------------------
CREATE TABLE HinhAnh (
    MaHinhAnh  INT IDENTITY(1,1) NOT NULL,
    MaBaoCao   INT               NOT NULL,
    DuongDan   NVARCHAR(260)     NOT NULL,
    NgayTai    DATETIME2(0)      NOT NULL CONSTRAINT DF_HinhAnh_NgayTai DEFAULT SYSDATETIME(),
    CONSTRAINT PK_HinhAnh PRIMARY KEY (MaHinhAnh),
    CONSTRAINT FK_HinhAnh_BaoCao FOREIGN KEY (MaBaoCao)
        REFERENCES BaoCaoHienTruong (MaBaoCao) ON DELETE CASCADE,
    CONSTRAINT UQ_HinhAnh_BaoCao_DuongDan UNIQUE (MaBaoCao, DuongDan)
);
GO

-- Mỗi báo cáo tối đa 5 ảnh (BR01)
CREATE TRIGGER TR_HinhAnh_ToiDa5
ON HinhAnh
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1
        FROM HinhAnh h
        JOIN (SELECT DISTINCT MaBaoCao FROM inserted) i ON h.MaBaoCao = i.MaBaoCao
        GROUP BY h.MaBaoCao
        HAVING COUNT(*) > 5
    )
    BEGIN
        RAISERROR (N'Mỗi báo cáo chỉ được có tối đa 5 ảnh.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO

-- 8. LỊCH SỬ XÁC MINH ------------------------------------------
CREATE TABLE LichSuXacMinh (
    MaLichSu         INT IDENTITY(1,1) NOT NULL,
    MaBaoCao         INT               NOT NULL,
    MaNguoiThucHien  INT               NULL,      -- NULL = hệ thống tự làm
    MaTrangThaiCu    VARCHAR(20)       NULL,      -- NULL ở dòng đầu tiên
    MaTrangThaiMoi   VARCHAR(20)       NOT NULL,
    GhiChu           NVARCHAR(500)     NULL,
    ThoiGian         DATETIME2(0)      NOT NULL CONSTRAINT DF_LichSu_ThoiGian DEFAULT SYSDATETIME(),
    CONSTRAINT PK_LichSuXacMinh PRIMARY KEY (MaLichSu),
    CONSTRAINT FK_LichSu_BaoCao   FOREIGN KEY (MaBaoCao)        REFERENCES BaoCaoHienTruong (MaBaoCao),
    CONSTRAINT FK_LichSu_NguoiDung FOREIGN KEY (MaNguoiThucHien) REFERENCES NguoiDung (MaNguoiDung),
    CONSTRAINT FK_LichSu_TrangThaiCu  FOREIGN KEY (MaTrangThaiCu)  REFERENCES TrangThaiBaoCao (MaTrangThai),
    CONSTRAINT FK_LichSu_TrangThaiMoi FOREIGN KEY (MaTrangThaiMoi) REFERENCES TrangThaiBaoCao (MaTrangThai),
    -- Từ chối phải có lý do (BR04)
    CONSTRAINT CK_LichSu_BacBo_CoLyDo CHECK (
        MaTrangThaiMoi <> 'BI_BAC_BO'
        OR (GhiChu IS NOT NULL AND LEN(LTRIM(RTRIM(GhiChu))) > 0)
    )
);

-- 9. MÔ HÌNH AI VÀ DỰ BÁO NGUY CƠ (BR08, ARCH05) --------------
CREATE TABLE MoHinhAI (
    MaMoHinh        INT IDENTITY(1,1) NOT NULL,
    PhienBan        VARCHAR(20)       NOT NULL,
    DuongDanFile    NVARCHAR(260)     NOT NULL,
    NgayHuanLuyen   DATETIME2(0)      NOT NULL CONSTRAINT DF_MoHinh_Ngay DEFAULT SYSDATETIME(),
    SoMauHuanLuyen  INT               NOT NULL,
    Accuracy        DECIMAL(5,4)      NOT NULL,
    F1              DECIMAL(5,4)      NOT NULL,
    AUC             DECIMAL(5,4)      NOT NULL,
    DangSuDung      BIT               NOT NULL CONSTRAINT DF_MoHinh_DangSuDung DEFAULT 0,
    CONSTRAINT PK_MoHinhAI PRIMARY KEY (MaMoHinh),
    CONSTRAINT UQ_MoHinhAI_PhienBan UNIQUE (PhienBan),
    CONSTRAINT CK_MoHinh_ChiSo CHECK (Accuracy BETWEEN 0 AND 1 AND F1 BETWEEN 0 AND 1 AND AUC BETWEEN 0 AND 1),
    CONSTRAINT CK_MoHinh_SoMau CHECK (SoMauHuanLuyen > 0)
);
GO
-- Tại một thời điểm chỉ một phiên bản được dùng
CREATE UNIQUE INDEX UX_MoHinhAI_DangSuDung ON MoHinhAI (DangSuDung) WHERE DangSuDung = 1;
GO

CREATE TABLE DuBaoNguyCo (
    MaDuBao      INT IDENTITY(1,1) NOT NULL,
    MaBaoCao     INT               NOT NULL,
    MaMoHinh     INT               NOT NULL,
    XacSuat      DECIMAL(5,4)      NOT NULL,
    MucNguyCo    VARCHAR(10)       NOT NULL,
    SoNgayDuBao  TINYINT           NOT NULL CONSTRAINT DF_DuBao_SoNgay DEFAULT 7,
    NgayDuBao    DATETIME2(0)      NOT NULL CONSTRAINT DF_DuBao_Ngay DEFAULT SYSDATETIME(),
    CONSTRAINT PK_DuBaoNguyCo PRIMARY KEY (MaDuBao),
    -- Liên kết 1 - 0..1 với báo cáo: khóa ngoại + UNIQUE
    CONSTRAINT UQ_DuBao_BaoCao UNIQUE (MaBaoCao),
    CONSTRAINT FK_DuBao_BaoCao FOREIGN KEY (MaBaoCao) REFERENCES BaoCaoHienTruong (MaBaoCao),
    CONSTRAINT FK_DuBao_MoHinh FOREIGN KEY (MaMoHinh) REFERENCES MoHinhAI (MaMoHinh),
    CONSTRAINT CK_DuBao_XacSuat CHECK (XacSuat BETWEEN 0 AND 1),
    CONSTRAINT CK_DuBao_Muc CHECK (MucNguyCo IN ('THAP', 'TRUNG_BINH', 'CAO')),
    CONSTRAINT CK_DuBao_SoNgay CHECK (SoNgayDuBao > 0)
);

-- 10. CẢNH BÁO ------------------------------------------------
CREATE TABLE CanhBao (
    MaCanhBao  INT IDENTITY(1,1) NOT NULL,
    MaKhuVuc   INT               NOT NULL,
    MaLoai     INT               NOT NULL,
    MaDuBao    INT               NULL,      -- NULL = cảnh báo theo BR06
    NoiDung    NVARCHAR(500)     NOT NULL,
    NgayTao    DATETIME2(0)      NOT NULL CONSTRAINT DF_CanhBao_NgayTao DEFAULT SYSDATETIME(),
    CONSTRAINT PK_CanhBao PRIMARY KEY (MaCanhBao),
    CONSTRAINT FK_CanhBao_KhuVuc FOREIGN KEY (MaKhuVuc) REFERENCES KhuVuc (MaKhuVuc),
    CONSTRAINT FK_CanhBao_Loai   FOREIGN KEY (MaLoai)   REFERENCES LoaiSauBenh (MaLoai),
    CONSTRAINT FK_CanhBao_DuBao  FOREIGN KEY (MaDuBao)  REFERENCES DuBaoNguyCo (MaDuBao)
);

-- 11. THÔNG BÁO NỘI BỘ ----------------------------------------
CREATE TABLE ThongBao (
    MaThongBao   INT IDENTITY(1,1) NOT NULL,
    MaNguoiNhan  INT               NOT NULL,
    MaBaoCao     INT               NULL,
    MaLoaiTB     VARCHAR(20)       NOT NULL,
    TieuDe       NVARCHAR(200)     NOT NULL,
    NoiDung      NVARCHAR(1000)    NOT NULL,
    NgayTao      DATETIME2(0)      NOT NULL CONSTRAINT DF_ThongBao_NgayTao DEFAULT SYSDATETIME(),
    DaDoc        BIT               NOT NULL CONSTRAINT DF_ThongBao_DaDoc DEFAULT 0,
    CONSTRAINT PK_ThongBao PRIMARY KEY (MaThongBao),
    CONSTRAINT FK_ThongBao_NguoiNhan FOREIGN KEY (MaNguoiNhan) REFERENCES NguoiDung (MaNguoiDung),
    CONSTRAINT FK_ThongBao_BaoCao    FOREIGN KEY (MaBaoCao)    REFERENCES BaoCaoHienTruong (MaBaoCao),
    CONSTRAINT FK_ThongBao_Loai      FOREIGN KEY (MaLoaiTB)    REFERENCES LoaiThongBao (MaLoaiTB)
);
GO

-- 12. CHỈ MỤC (index) cho các truy vấn hay dùng --------------
CREATE INDEX IX_BaoCao_TrangThai_KhuVuc_NgayGui
    ON BaoCaoHienTruong (MaTrangThai, MaKhuVuc, NgayGui);
CREATE INDEX IX_BaoCao_NguoiGui
    ON BaoCaoHienTruong (MaNguoiGui);
CREATE INDEX IX_BaoCao_KhuVuc_Loai
    ON BaoCaoHienTruong (MaKhuVuc, MaLoai);
CREATE INDEX IX_HinhAnh_BaoCao
    ON HinhAnh (MaBaoCao);
CREATE INDEX IX_LichSu_BaoCao
    ON LichSuXacMinh (MaBaoCao);
CREATE INDEX IX_ThongBao_NguoiNhan_DaDoc
    ON ThongBao (MaNguoiNhan, DaDoc);
-- Kiểm tra cảnh báo trùng trong SO_NGAY_XET_NGUONG ngày (BR06, BR08)
CREATE INDEX IX_CanhBao_KhuVuc_Loai_NgayTao
    ON CanhBao (MaKhuVuc, MaLoai, NgayTao);
CREATE INDEX IX_DuBao_MoHinh
    ON DuBaoNguyCo (MaMoHinh);
-- Liên kết 0..1 - 0..1 giữa dự báo và cảnh báo: nhiều dòng NULL vẫn hợp lệ
CREATE UNIQUE INDEX UX_CanhBao_MaDuBao
    ON CanhBao (MaDuBao) WHERE MaDuBao IS NOT NULL;
GO

-- 13. DỮ LIỆU KHỞI TẠO ----------------------------------------
INSERT INTO VaiTro (MaVaiTro, TenVaiTro) VALUES
    ('NONG_DAN',   N'Nông dân'),
    ('CHUYEN_GIA', N'Chuyên gia / Nhân viên kiểm định'),
    ('QUAN_TRI',   N'Quản trị viên');

INSERT INTO TrangThaiBaoCao (MaTrangThai, TenTrangThai, ThuTu) VALUES
    ('CHO_XAC_MINH', N'Chờ xác minh', 1),
    ('DA_XAC_MINH',  N'Đã xác minh',  2),
    ('BI_BAC_BO',    N'Bị bác bỏ',    3),
    ('DA_XU_LY',     N'Đã xử lý',     4);

INSERT INTO LoaiThongBao (MaLoaiTB, TenLoaiTB) VALUES
    ('BAO_CAO_MOI',      N'Báo cáo mới'),
    ('KET_QUA_XAC_MINH', N'Kết quả xác minh'),
    ('QUA_HAN',          N'Báo cáo quá hạn'),
    ('DONG_HO_SO',       N'Đóng hồ sơ'),
    ('CANH_BAO_DICH',    N'Cảnh báo dịch hại');

INSERT INTO ThamSoHeThong (MaThamSo, GiaTri, MoTa) VALUES
    ('HAN_XAC_MINH_GIO',   48, N'Số giờ chuyên gia phải xử lý báo cáo (BR02)'),
    ('NGUONG_CANH_BAO',     5, N'Số báo cáo đã xác minh để tạo cảnh báo dịch (BR06)'),
    ('SO_NGAY_XET_NGUONG',  7, N'Số ngày tính khi đếm báo cáo để so với ngưỡng (BR06)'),
    ('HAN_GUI_LAI_NGAY',    7, N'Số ngày nông dân được gửi lại báo cáo bị bác bỏ (BR04)'),
    ('NGUONG_NGUY_CO',     70, N'Ngưỡng xác suất (%) để dự báo được xếp mức Cao (BR08)');
GO

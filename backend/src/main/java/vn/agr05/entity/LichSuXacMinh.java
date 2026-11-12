package vn.agr05.entity;

import java.time.LocalDateTime;

/** Một dòng nhật ký đổi trạng thái (bảng LichSuXacMinh). */
public class LichSuXacMinh {

    private Integer maLichSu;
    private Integer maBaoCao;
    private Integer maNguoiThucHien;
    private String trangThaiCu;
    private String trangThaiMoi;
    private String ghiChu;
    private LocalDateTime thoiGian;
    /** Họ tên người thực hiện (lấy kèm khi đọc). */
    private String tenNguoiThucHien;

    public Integer getMaLichSu() {
        return maLichSu;
    }

    public void setMaLichSu(Integer maLichSu) {
        this.maLichSu = maLichSu;
    }

    public Integer getMaBaoCao() {
        return maBaoCao;
    }

    public void setMaBaoCao(Integer maBaoCao) {
        this.maBaoCao = maBaoCao;
    }

    public Integer getMaNguoiThucHien() {
        return maNguoiThucHien;
    }

    public void setMaNguoiThucHien(Integer maNguoiThucHien) {
        this.maNguoiThucHien = maNguoiThucHien;
    }

    public String getTrangThaiCu() {
        return trangThaiCu;
    }

    public void setTrangThaiCu(String trangThaiCu) {
        this.trangThaiCu = trangThaiCu;
    }

    public String getTrangThaiMoi() {
        return trangThaiMoi;
    }

    public void setTrangThaiMoi(String trangThaiMoi) {
        this.trangThaiMoi = trangThaiMoi;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }

    public String getTenNguoiThucHien() {
        return tenNguoiThucHien;
    }

    public void setTenNguoiThucHien(String tenNguoiThucHien) {
        this.tenNguoiThucHien = tenNguoiThucHien;
    }
}

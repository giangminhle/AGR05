package vn.agr05.entity;

import java.time.LocalDateTime;

/** Ảnh hiện trường, chỉ lưu đường dẫn file (bảng HinhAnh). */
public class HinhAnh {

    private Integer maHinhAnh;
    private Integer maBaoCao;
    private String duongDan;
    private LocalDateTime ngayTai;

    public Integer getMaHinhAnh() {
        return maHinhAnh;
    }

    public void setMaHinhAnh(Integer maHinhAnh) {
        this.maHinhAnh = maHinhAnh;
    }

    public Integer getMaBaoCao() {
        return maBaoCao;
    }

    public void setMaBaoCao(Integer maBaoCao) {
        this.maBaoCao = maBaoCao;
    }

    public String getDuongDan() {
        return duongDan;
    }

    public void setDuongDan(String duongDan) {
        this.duongDan = duongDan;
    }

    public LocalDateTime getNgayTai() {
        return ngayTai;
    }

    public void setNgayTai(LocalDateTime ngayTai) {
        this.ngayTai = ngayTai;
    }
}

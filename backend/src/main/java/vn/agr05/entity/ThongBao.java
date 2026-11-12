package vn.agr05.entity;

import java.time.LocalDateTime;

/** Thông báo nội bộ (bảng ThongBao). */
public class ThongBao {

    private Integer maThongBao;
    private Integer maNguoiNhan;
    private Integer maBaoCao;
    private String loai;
    private String tieuDe;
    private String noiDung;
    private LocalDateTime ngayTao;
    private boolean daDoc;

    public Integer getMaThongBao() {
        return maThongBao;
    }

    public void setMaThongBao(Integer maThongBao) {
        this.maThongBao = maThongBao;
    }

    public Integer getMaNguoiNhan() {
        return maNguoiNhan;
    }

    public void setMaNguoiNhan(Integer maNguoiNhan) {
        this.maNguoiNhan = maNguoiNhan;
    }

    public Integer getMaBaoCao() {
        return maBaoCao;
    }

    public void setMaBaoCao(Integer maBaoCao) {
        this.maBaoCao = maBaoCao;
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public boolean isDaDoc() {
        return daDoc;
    }

    public void setDaDoc(boolean daDoc) {
        this.daDoc = daDoc;
    }
}

package vn.agr05.entity;

import java.time.LocalDateTime;

/** Cảnh báo dịch theo khu vực và loại (bảng CanhBao). */
public class CanhBao {

    private Integer maCanhBao;
    private Integer maKhuVuc;
    private Integer maLoai;
    private Integer maDuBao;
    private String noiDung;
    private LocalDateTime ngayTao;
    /** Lấy kèm khi đọc. */
    private String tenKhuVuc;
    /** Lấy kèm khi đọc. */
    private String tenLoai;

    public Integer getMaCanhBao() {
        return maCanhBao;
    }

    public void setMaCanhBao(Integer maCanhBao) {
        this.maCanhBao = maCanhBao;
    }

    public Integer getMaKhuVuc() {
        return maKhuVuc;
    }

    public void setMaKhuVuc(Integer maKhuVuc) {
        this.maKhuVuc = maKhuVuc;
    }

    public Integer getMaLoai() {
        return maLoai;
    }

    public void setMaLoai(Integer maLoai) {
        this.maLoai = maLoai;
    }

    public Integer getMaDuBao() {
        return maDuBao;
    }

    public void setMaDuBao(Integer maDuBao) {
        this.maDuBao = maDuBao;
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

    public String getTenKhuVuc() {
        return tenKhuVuc;
    }

    public void setTenKhuVuc(String tenKhuVuc) {
        this.tenKhuVuc = tenKhuVuc;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }
}

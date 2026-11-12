package vn.agr05.entity;

import java.time.LocalDateTime;

/** Kết quả một lần dự báo nguy cơ dịch (bảng DuBaoNguyCo). */
public class DuBaoNguyCo {

    private Integer maDuBao;
    private Integer maBaoCao;
    private Integer maMoHinh;
    private double xacSuat;
    private String mucNguyCo;
    private int soNgayDuBao = 7;
    private LocalDateTime ngayDuBao;

    public Integer getMaDuBao() {
        return maDuBao;
    }

    public void setMaDuBao(Integer maDuBao) {
        this.maDuBao = maDuBao;
    }

    public Integer getMaBaoCao() {
        return maBaoCao;
    }

    public void setMaBaoCao(Integer maBaoCao) {
        this.maBaoCao = maBaoCao;
    }

    public Integer getMaMoHinh() {
        return maMoHinh;
    }

    public void setMaMoHinh(Integer maMoHinh) {
        this.maMoHinh = maMoHinh;
    }

    public double getXacSuat() {
        return xacSuat;
    }

    public void setXacSuat(double xacSuat) {
        this.xacSuat = xacSuat;
    }

    public String getMucNguyCo() {
        return mucNguyCo;
    }

    public void setMucNguyCo(String mucNguyCo) {
        this.mucNguyCo = mucNguyCo;
    }

    public int getSoNgayDuBao() {
        return soNgayDuBao;
    }

    public void setSoNgayDuBao(int soNgayDuBao) {
        this.soNgayDuBao = soNgayDuBao;
    }

    public LocalDateTime getNgayDuBao() {
        return ngayDuBao;
    }

    public void setNgayDuBao(LocalDateTime ngayDuBao) {
        this.ngayDuBao = ngayDuBao;
    }

    /** Trả về true khi mức nguy cơ là Cao. */
    public boolean laNguyCoCao() {
        return MucNguyCo.CAO.equals(mucNguyCo);
    }
}

package vn.agr05.entity;

import java.time.LocalDateTime;

/** Tài khoản đăng nhập (bảng NguoiDung). */
public class NguoiDung {

    private Integer maNguoiDung;
    private String tenDangNhap;
    private String matKhauHash;
    private String muoi;
    private String hoTen;
    private String dienThoai;
    private String vaiTro;
    private boolean biKhoa;
    private LocalDateTime ngayTao;

    public Integer getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(Integer maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhauHash() {
        return matKhauHash;
    }

    public void setMatKhauHash(String matKhauHash) {
        this.matKhauHash = matKhauHash;
    }

    public String getMuoi() {
        return muoi;
    }

    public void setMuoi(String muoi) {
        this.muoi = muoi;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getDienThoai() {
        return dienThoai;
    }

    public void setDienThoai(String dienThoai) {
        this.dienThoai = dienThoai;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public boolean isBiKhoa() {
        return biKhoa;
    }

    public void setBiKhoa(boolean biKhoa) {
        this.biKhoa = biKhoa;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    /** Băm mật khẩu nhập vào với muối đã lưu rồi so với MatKhauHash (SEC01). */
    public boolean xacThucMatKhau(String mk) {
        if (mk == null || matKhauHash == null || muoi == null) {
            return false;
        }
        return MatKhau.khop(mk, muoi, matKhauHash);
    }
}

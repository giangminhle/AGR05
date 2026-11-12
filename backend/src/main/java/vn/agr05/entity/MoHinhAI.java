package vn.agr05.entity;

import java.time.LocalDateTime;

/** Một phiên bản mô hình AI đã huấn luyện (bảng MoHinhAI). */
public class MoHinhAI {

    private Integer maMoHinh;
    private String phienBan;
    private String duongDanFile;
    private LocalDateTime ngayHuanLuyen;
    private int soMauHuanLuyen;
    private double accuracy;
    private double f1;
    private double auc;
    private boolean dangSuDung;

    public Integer getMaMoHinh() {
        return maMoHinh;
    }

    public void setMaMoHinh(Integer maMoHinh) {
        this.maMoHinh = maMoHinh;
    }

    public String getPhienBan() {
        return phienBan;
    }

    public void setPhienBan(String phienBan) {
        this.phienBan = phienBan;
    }

    public String getDuongDanFile() {
        return duongDanFile;
    }

    public void setDuongDanFile(String duongDanFile) {
        this.duongDanFile = duongDanFile;
    }

    public LocalDateTime getNgayHuanLuyen() {
        return ngayHuanLuyen;
    }

    public void setNgayHuanLuyen(LocalDateTime ngayHuanLuyen) {
        this.ngayHuanLuyen = ngayHuanLuyen;
    }

    public int getSoMauHuanLuyen() {
        return soMauHuanLuyen;
    }

    public void setSoMauHuanLuyen(int soMauHuanLuyen) {
        this.soMauHuanLuyen = soMauHuanLuyen;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getF1() {
        return f1;
    }

    public void setF1(double f1) {
        this.f1 = f1;
    }

    public double getAuc() {
        return auc;
    }

    public void setAuc(double auc) {
        this.auc = auc;
    }

    public boolean isDangSuDung() {
        return dangSuDung;
    }

    public void setDangSuDung(boolean dangSuDung) {
        this.dangSuDung = dangSuDung;
    }

    /** ARCH05: bản này không kém bản mh nếu F1 lớn hơn hoặc bằng. mh rỗng (chưa có bản đang dùng) thì luôn đúng. */
    public boolean khongKemHon(MoHinhAI mh) {
        return mh == null || f1 >= mh.getF1();
    }
}

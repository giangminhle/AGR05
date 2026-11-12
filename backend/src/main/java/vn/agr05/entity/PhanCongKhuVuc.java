package vn.agr05.entity;

import java.time.LocalDateTime;

/** Lớp liên kết chuyên gia - khu vực (bảng PhanCongKhuVuc). */
public class PhanCongKhuVuc {

    private Integer maNguoiDung;
    private Integer maKhuVuc;
    private LocalDateTime ngayPhanCong;

    public Integer getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(Integer maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public Integer getMaKhuVuc() {
        return maKhuVuc;
    }

    public void setMaKhuVuc(Integer maKhuVuc) {
        this.maKhuVuc = maKhuVuc;
    }

    public LocalDateTime getNgayPhanCong() {
        return ngayPhanCong;
    }

    public void setNgayPhanCong(LocalDateTime ngayPhanCong) {
        this.ngayPhanCong = ngayPhanCong;
    }
}

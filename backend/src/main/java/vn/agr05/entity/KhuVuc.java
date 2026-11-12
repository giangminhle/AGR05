package vn.agr05.entity;

/** Vùng canh tác (bảng KhuVuc). */
public class KhuVuc {

    private Integer maKhuVuc;
    private String tenKhuVuc;
    private String moTa;
    /** Số chuyên gia phụ trách (đếm từ PhanCongKhuVuc, không phải cột). */
    private int soChuyenGia;

    public Integer getMaKhuVuc() {
        return maKhuVuc;
    }

    public void setMaKhuVuc(Integer maKhuVuc) {
        this.maKhuVuc = maKhuVuc;
    }

    public String getTenKhuVuc() {
        return tenKhuVuc;
    }

    public void setTenKhuVuc(String tenKhuVuc) {
        this.tenKhuVuc = tenKhuVuc;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public int getSoChuyenGia() {
        return soChuyenGia;
    }

    public void setSoChuyenGia(int soChuyenGia) {
        this.soChuyenGia = soChuyenGia;
    }
}

package vn.agr05.entity;

/** Danh mục sâu bệnh (bảng LoaiSauBenh). */
public class LoaiSauBenh {

    private Integer maLoai;
    private String tenLoai;
    private String trieuChung;
    private String bienPhapPhongTru;

    public Integer getMaLoai() {
        return maLoai;
    }

    public void setMaLoai(Integer maLoai) {
        this.maLoai = maLoai;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    public String getTrieuChung() {
        return trieuChung;
    }

    public void setTrieuChung(String trieuChung) {
        this.trieuChung = trieuChung;
    }

    public String getBienPhapPhongTru() {
        return bienPhapPhongTru;
    }

    public void setBienPhapPhongTru(String bienPhapPhongTru) {
        this.bienPhapPhongTru = bienPhapPhongTru;
    }
}

package vn.agr05.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import vn.agr05.entity.BaoCaoHienTruong;

/** Các ô của BaoCaoHienTruongPage gửi lên dạng multipart/form-data. */
public class BaoCaoForm {

    private String tieuDe;
    private String tenCayTrong;
    private String moTa;
    private Integer mucDo;
    private BigDecimal viDo;
    private BigDecimal kinhDo;
    private Integer maKhuVuc;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngayPhatHien;

    BaoCaoHienTruong sangEntity() {
        BaoCaoHienTruong bc = new BaoCaoHienTruong();
        bc.setTieuDe(tieuDe);
        bc.setTenCayTrong(tenCayTrong);
        bc.setMoTa(moTa);
        bc.setMucDo(mucDo);
        bc.setViDo(viDo);
        bc.setKinhDo(kinhDo);
        bc.setMaKhuVuc(maKhuVuc);
        bc.setNgayPhatHien(ngayPhatHien);
        return bc;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getTenCayTrong() {
        return tenCayTrong;
    }

    public void setTenCayTrong(String tenCayTrong) {
        this.tenCayTrong = tenCayTrong;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Integer getMucDo() {
        return mucDo;
    }

    public void setMucDo(Integer mucDo) {
        this.mucDo = mucDo;
    }

    public BigDecimal getViDo() {
        return viDo;
    }

    public void setViDo(BigDecimal viDo) {
        this.viDo = viDo;
    }

    public BigDecimal getKinhDo() {
        return kinhDo;
    }

    public void setKinhDo(BigDecimal kinhDo) {
        this.kinhDo = kinhDo;
    }

    public Integer getMaKhuVuc() {
        return maKhuVuc;
    }

    public void setMaKhuVuc(Integer maKhuVuc) {
        this.maKhuVuc = maKhuVuc;
    }

    public LocalDate getNgayPhatHien() {
        return ngayPhatHien;
    }

    public void setNgayPhatHien(LocalDate ngayPhatHien) {
        this.ngayPhatHien = ngayPhatHien;
    }
}

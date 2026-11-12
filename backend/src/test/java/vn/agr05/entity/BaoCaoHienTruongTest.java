package vn.agr05.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BaoCaoHienTruongTest {

    static BaoCaoHienTruong mau() {
        BaoCaoHienTruong bc = new BaoCaoHienTruong();
        bc.setTieuDe("t");
        bc.setTenCayTrong("Lúa");
        bc.setMoTa("m");
        bc.setMucDo(1);
        bc.setViDo(new BigDecimal("10"));
        bc.setKinhDo(new BigDecimal("106"));
        bc.setNgayPhatHien(LocalDate.now());
        bc.setMaKhuVuc(1);
        bc.setMaNguoiGui(5);
        return bc;
    }

    @ParameterizedTest
    @CsvSource({"0,anhThieu", "1,", "5,", "6,anhThua"})
    void kiemTraHopLe_bienSoAnh(int soAnh, String ketQua) {
        assertThat(mau().kiemTraHopLe(soAnh)).isEqualTo(ketQua);
    }

    @ParameterizedTest
    @CsvSource({"90,", "-90,", "95,viDo", "-90.000001,viDo"})
    void kiemTraHopLe_bienViDo(String viDo, String ketQua) {
        BaoCaoHienTruong bc = mau();
        bc.setViDo(new BigDecimal(viDo));
        assertThat(bc.kiemTraHopLe(1)).isEqualTo(ketQua);
    }

    @Test
    void kiemTraHopLe_ngayPhatHienSauHomNay_sai() {
        BaoCaoHienTruong bc = mau();
        bc.setNgayPhatHien(LocalDate.now().plusDays(1));
        assertThat(bc.kiemTraHopLe(1)).isEqualTo("ngayPhatHien");
    }

    @Test
    void chuyenTrangThai_dungThuTu() {
        BaoCaoHienTruong bc = mau();
        bc.chuyenTrangThai(TrangThai.CHO_XAC_MINH);
        bc.chuyenTrangThai(TrangThai.DA_XAC_MINH);
        bc.chuyenTrangThai(TrangThai.DA_XU_LY);
        assertThat(bc.getTrangThai()).isEqualTo(TrangThai.DA_XU_LY);
    }

    @Test
    void chuyenTrangThai_nhayBuoc_bao_loi() {
        BaoCaoHienTruong bc = mau();
        bc.setTrangThai(TrangThai.CHO_XAC_MINH);
        assertThatThrownBy(() -> bc.chuyenTrangThai(TrangThai.DA_XU_LY)).isInstanceOf(NghiepVuException.class);
        bc.setTrangThai(TrangThai.DA_XU_LY);
        assertThatThrownBy(() -> bc.chuyenTrangThai(TrangThai.CHO_XAC_MINH)).isInstanceOf(NghiepVuException.class);
    }

    @Test
    void laQuaHan_sau48Gio() {
        BaoCaoHienTruong bc = mau();
        bc.setTrangThai(TrangThai.CHO_XAC_MINH);
        bc.setNgayVaoTrangThai(LocalDateTime.now().minusHours(47));
        assertThat(bc.laQuaHan(48)).isFalse();
        bc.setNgayVaoTrangThai(LocalDateTime.now().minusHours(49));
        assertThat(bc.laQuaHan(48)).isTrue();
    }

    @Test
    void choPhepSua_theoTrangThaiVaNguoiGui() {
        BaoCaoHienTruong bc = mau();
        bc.setTrangThai(TrangThai.CHO_XAC_MINH);
        bc.setNgayVaoTrangThai(LocalDateTime.now());
        assertThat(bc.choPhepSua(5, 7)).isTrue();
        assertThat(bc.choPhepSua(6, 7)).isFalse();
        bc.setTrangThai(TrangThai.BI_BAC_BO);
        assertThat(bc.choPhepSua(5, 7)).isTrue();
        bc.setSoLanGuiLai(1);
        assertThat(bc.choPhepSua(5, 7)).isFalse();
        bc.setSoLanGuiLai(0);
        bc.setNgayVaoTrangThai(LocalDateTime.now().minusDays(8));
        assertThat(bc.choPhepSua(5, 7)).isFalse();
        bc.setTrangThai(TrangThai.DA_XAC_MINH);
        assertThat(bc.choPhepSua(5, 7)).isFalse();
    }

    @Test
    void matKhau_xacThuc() {
        NguoiDung nd = new NguoiDung();
        nd.setMuoi(MatKhau.taoMuoi());
        nd.setMatKhauHash(MatKhau.bam("abc", nd.getMuoi()));
        assertThat(nd.xacThucMatKhau("abc")).isTrue();
        assertThat(nd.xacThucMatKhau("abd")).isFalse();
        assertThat(MatKhau.taoMuoi()).isNotEqualTo(MatKhau.taoMuoi());
    }

    @Test
    void duBao_vaMoHinh() {
        DuBaoNguyCo d = new DuBaoNguyCo();
        d.setMucNguyCo(MucNguyCo.CAO);
        assertThat(d.laNguyCoCao()).isTrue();
        MoHinhAI a = new MoHinhAI();
        a.setF1(0.8);
        MoHinhAI b = new MoHinhAI();
        b.setF1(0.79);
        assertThat(a.khongKemHon(b)).isTrue();
        assertThat(b.khongKemHon(a)).isFalse();
        assertThat(b.khongKemHon(null)).isTrue();
    }
}

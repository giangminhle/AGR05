package vn.agr05.bll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.agr05.entity.BaoCaoHienTruong;
import vn.agr05.entity.DuBaoNguyCo;
import vn.agr05.entity.MoHinhAI;
import vn.agr05.entity.MucNguyCo;
import vn.agr05.entity.NguoiDung;
import vn.agr05.entity.TrangThai;
import vn.agr05.entity.VaiTro;

class AIServiceTest {

    @TempDir
    Path tam;

    HeThongGia ht;
    NguoiDung nongDan;
    NguoiDung cg;
    int kv;
    int loai;

    @BeforeEach
    void chuanBi() {
        ht = new HeThongGia(tam);
        nongDan = ht.db.themNguoiDung("nd", VaiTro.NONG_DAN, "123456");
        cg = ht.db.themNguoiDung("cg", VaiTro.CHUYEN_GIA, "123456");
        kv = ht.db.themKhuVuc("Xã A").getMaKhuVuc();
        ht.db.phanCong(cg.getMaNguoiDung(), kv);
        loai = ht.db.themLoai("Rầy nâu").getMaLoai();
    }

    MoHinhAI themMoHinh(double f1, boolean dangDung) {
        MoHinhAI mh = new MoHinhAI();
        mh.setPhienBan("v" + ht.db.moHinh.size());
        mh.setDuongDanFile("mh.model");
        mh.setF1(f1);
        mh.setDangSuDung(dangDung);
        ht.db.moHinhDal.insert(mh);
        return mh;
    }

    int duyetMot() {
        BaoCaoHienTruong bc = ht.db.themBaoCao(nongDan.getMaNguoiDung(), kv, TrangThai.CHO_XAC_MINH);
        ht.xacMinh.xacMinh(bc.getMaBaoCao(), loai, null, cg.getMaNguoiDung());
        return bc.getMaBaoCao();
    }

    @Test
    void TC11_duyet_khiChuaCoMoHinh_thanhCong_khongCoDuBao() {
        int ma = duyetMot();
        assertThat(ht.db.timBaoCao(ma).getTrangThai()).isEqualTo(TrangThai.DA_XAC_MINH);
        assertThat(ht.db.duBao).isEmpty();
    }

    @Test
    void TC11_moHinhLoiKhiNap_duyetVanThanhCong() {
        themMoHinh(0.8, true);
        ht.hocMay.loiKhiNap = true;
        int ma = duyetMot();
        assertThat(ht.db.timBaoCao(ma).getTrangThai()).isEqualTo(TrangThai.DA_XAC_MINH);
        assertThat(ht.db.duBao).isEmpty();
    }

    @Test
    void TC12_xacSuatBangNguong_mucCao_taoMotCanhBao() {
        themMoHinh(0.8, true);
        ht.hocMay.xacSuat = 0.70;
        int ma = duyetMot();

        DuBaoNguyCo d = ht.db.duBaoDal.getByBaoCao(ma);
        assertThat(d.getMucNguyCo()).isEqualTo(MucNguyCo.CAO);
        assertThat(ht.db.canhBao).singleElement().satisfies(c -> assertThat(c.getMaDuBao()).isEqualTo(d.getMaDuBao()));
    }

    @Test
    void TC13_haiLanDuBaoCao_cungLoaiCungKhuVuc_chiMotCanhBao() {
        themMoHinh(0.8, true);
        ht.hocMay.xacSuat = 0.9;
        duyetMot();
        duyetMot();
        assertThat(ht.db.duBao).hasSize(2);
        assertThat(ht.db.canhBao).hasSize(1);
    }

    @Test
    void xepMuc_theoNguong() {
        assertThat(ht.ai.xepMuc(0.69, 0.7)).isEqualTo(MucNguyCo.TRUNG_BINH);
        assertThat(ht.ai.xepMuc(0.34, 0.7)).isEqualTo(MucNguyCo.THAP);
        assertThat(ht.ai.xepMuc(0.7, 0.7)).isEqualTo(MucNguyCo.CAO);
    }

    @Test
    void TC14_huanLuyen_khiChiCoNhan0_baoChuaDuDuLieu_giuBanCu() {
        MoHinhAI cu = themMoHinh(0.6, true);
        // Ca lẻ tẻ, cách nhau 20 ngày: không bao giờ đủ 5 ca trong 7 ngày → toàn nhãn 0
        for (int i = 0; i < 12; i++) {
            ht.db.themCaDaXacMinh(nongDan.getMaNguoiDung(), kv, loai, LocalDateTime.now().minusDays(20L * i + 10));
        }
        assertThatThrownBy(() -> ht.ai.huanLuyenLai()).hasMessageContaining("Chưa đủ dữ liệu");
        assertThat(ht.db.moHinh).hasSize(1);
        assertThat(ht.db.moHinhDal.getDangSuDung().getMaMoHinh()).isEqualTo(cu.getMaMoHinh());
    }

    @Test
    void TC15_kichHoat_banMoiF1ThapHon_tuChoi() {
        MoHinhAI cu = themMoHinh(0.80, true);
        MoHinhAI moi = themMoHinh(0.75, false);
        assertThatThrownBy(() -> ht.ai.kichHoat(moi.getMaMoHinh())).hasMessageContaining("F1 thấp hơn");
        assertThat(ht.db.moHinhDal.getDangSuDung().getMaMoHinh()).isEqualTo(cu.getMaMoHinh());
    }

    @Test
    void kichHoat_banMoiF1BangNhau_duocKichHoat() {
        themMoHinh(0.80, true);
        MoHinhAI moi = themMoHinh(0.80, false);
        assertThat(ht.ai.kichHoat(moi.getMaMoHinh())).isTrue();
        assertThat(ht.db.moHinhDal.getDangSuDung().getMaMoHinh()).isEqualTo(moi.getMaMoHinh());
    }

    @Test
    void huanLuyen_duHaiNhan_luuPhienBanChuaDung() {
        taoDuLieuCoBungPhat();
        MoHinhAI mh = ht.ai.huanLuyenLai();
        assertThat(mh.isDangSuDung()).isFalse();
        assertThat(mh.getSoMauHuanLuyen()).isPositive();
        assertThat(ht.db.moHinh).hasSize(1);
    }

    @Test
    void smileGradientTreeBoost_hocDuocVaLuuNapFile() {
        AIService.SmileHocMay smile = new AIService.SmileHocMay();
        Random r = new Random(1);
        List<DacTrung> x = new ArrayList<>();
        int[] y = new int[300];
        for (int i = 0; i < 300; i++) {
            int soCa7 = r.nextInt(8);
            x.add(new DacTrung(soCa7, soCa7 + r.nextInt(3), soCa7 + r.nextInt(6), r.nextInt(5) - 2, 1 + r.nextDouble() * 2,
                    1 + r.nextInt(12), 1 + r.nextInt(2), 1 + r.nextInt(2)));
            y[i] = soCa7 >= 4 ? 1 : 0;
        }
        MoHinhDuBao mh = smile.huanLuyen(x, y, List.of(1, 2), List.of(1, 2));
        Path file = tam.resolve("thu.model");
        smile.luu(mh, file);
        MoHinhDuBao nap = smile.nap(file);

        DacTrung cao = new DacTrung(7, 8, 10, 3, 2.5, 6, 1, 1);
        DacTrung thap = new DacTrung(0, 0, 1, 0, 1, 6, 1, 1);
        assertThat(nap.xacSuat(cao)).isGreaterThan(0.5);
        assertThat(nap.xacSuat(thap)).isLessThan(0.5);
        AIService.ChiSoDanhGia cs = AIService.danhGia(nap, x, y);
        assertThat(cs.f1()).isGreaterThan(0.8);
        assertThat(cs.auc()).isGreaterThan(0.8);
    }

    /** Hai đợt bùng phát dày (mỗi ngày 1 ca) và nhiều ca lẻ tẻ: có cả nhãn 0 và 1 trong 80% mẫu cũ. */
    private void taoDuLieuCoBungPhat() {
        LocalDateTime goc = LocalDateTime.now().minusDays(200);
        for (int i = 0; i < 12; i++) {
            ht.db.themCaDaXacMinh(nongDan.getMaNguoiDung(), kv, loai, goc.plusDays(40 + i));
            ht.db.themCaDaXacMinh(nongDan.getMaNguoiDung(), kv, loai, goc.plusDays(150 + i));
        }
        for (int i = 0; i < 10; i++) {
            ht.db.themCaDaXacMinh(nongDan.getMaNguoiDung(), kv, loai, goc.plusDays(i * 9L));
            ht.db.themCaDaXacMinh(nongDan.getMaNguoiDung(), kv, loai, goc.plusDays(100 + i * 4L));
        }
    }
}

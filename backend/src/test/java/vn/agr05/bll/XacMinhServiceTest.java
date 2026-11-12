package vn.agr05.bll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.agr05.entity.BaoCaoHienTruong;
import vn.agr05.entity.LoaiThongBao;
import vn.agr05.entity.NghiepVuException;
import vn.agr05.entity.NguoiDung;
import vn.agr05.entity.TrangThai;
import vn.agr05.entity.VaiTro;

class XacMinhServiceTest {

    @TempDir
    Path tam;

    HeThongGia ht;
    NguoiDung nongDan;
    NguoiDung cgA;
    NguoiDung cgB;
    int kvA;
    int kvB;
    int maLoai;

    @BeforeEach
    void chuanBi() {
        ht = new HeThongGia(tam);
        nongDan = ht.db.themNguoiDung("nd", VaiTro.NONG_DAN, "123456");
        cgA = ht.db.themNguoiDung("cgA", VaiTro.CHUYEN_GIA, "123456");
        cgB = ht.db.themNguoiDung("cgB", VaiTro.CHUYEN_GIA, "123456");
        kvA = ht.db.themKhuVuc("Xã A").getMaKhuVuc();
        kvB = ht.db.themKhuVuc("Xã B").getMaKhuVuc();
        ht.db.phanCong(cgA.getMaNguoiDung(), kvA);
        ht.db.phanCong(cgB.getMaNguoiDung(), kvB);
        maLoai = ht.db.themLoai("Rầy nâu").getMaLoai();
    }

    BaoCaoHienTruong choXacMinh() {
        return ht.db.themBaoCao(nongDan.getMaNguoiDung(), kvA, TrangThai.CHO_XAC_MINH);
    }

    @Test
    void TC05_duyet_khiChuaChonLoai_baoLoi_trangThaiKhongDoi() {
        BaoCaoHienTruong bc = choXacMinh();
        assertThatThrownBy(() -> ht.xacMinh.xacMinh(bc.getMaBaoCao(), null, "kn", cgA.getMaNguoiDung()))
                .isInstanceOfSatisfying(NghiepVuException.class, e -> assertThat(e.getTruong()).isEqualTo("maLoai"));
        assertThat(ht.db.timBaoCao(bc.getMaBaoCao()).getTrangThai()).isEqualTo(TrangThai.CHO_XAC_MINH);
    }

    @Test
    void TC06_tuChoi_lyDoTrong_baoLoi() {
        BaoCaoHienTruong bc = choXacMinh();
        assertThatThrownBy(() -> ht.xacMinh.tuChoi(bc.getMaBaoCao(), "   ", cgA.getMaNguoiDung()))
                .hasMessageContaining("lý do");
        assertThat(ht.db.timBaoCao(bc.getMaBaoCao()).getTrangThai()).isEqualTo(TrangThai.CHO_XAC_MINH);
    }

    @Test
    void TC07_chuyenGiaKhuVucB_duyetBaoCaoKhuVucA_khongDuQuyen() {
        BaoCaoHienTruong bc = choXacMinh();
        assertThatThrownBy(() -> ht.xacMinh.xacMinh(bc.getMaBaoCao(), maLoai, null, cgB.getMaNguoiDung()))
                .isInstanceOfSatisfying(NghiepVuException.class,
                        e -> assertThat(e.getLoai()).isEqualTo(NghiepVuException.Loai.KHONG_DU_QUYEN));
        assertThat(ht.db.timBaoCao(bc.getMaBaoCao()).getTrangThai()).isEqualTo(TrangThai.CHO_XAC_MINH);
        assertThat(ht.db.lichSu).isEmpty();
    }

    @Test
    void TC08_haiChuyenGiaCungDuyet_nguoiSauNhanDaXuLy() {
        NguoiDung cgA2 = ht.db.themNguoiDung("cgA2", VaiTro.CHUYEN_GIA, "123456");
        ht.db.phanCong(cgA2.getMaNguoiDung(), kvA);
        BaoCaoHienTruong bc = choXacMinh();
        // Người sau đã đọc báo cáo lúc còn Chờ; người trước duyệt xong ngay trước câu UPDATE của người sau
        XacMinhService sv = new XacMinhService(new vn.agr05.fake.BaoCaoDalUyQuyen(ht.db.baoCaoDal) {
            @Override
            public boolean updateTrangThai(BaoCaoHienTruong b, String ttCu) {
                ht.xacMinh.xacMinh(bc.getMaBaoCao(), maLoai, "người trước", cgA.getMaNguoiDung());
                return super.updateTrangThai(b, ttCu);
            }
        }, ht.db.hinhAnhDal, ht.db.lichSuDal, ht.db.nguoiDungDal, ht.db.loaiDal, ht.thongBao, ht.canhBao, ht.thamSo,
                ht.ai, ht.txm.template());

        assertThatThrownBy(() -> sv.xacMinh(bc.getMaBaoCao(), maLoai, "người sau", cgA2.getMaNguoiDung()))
                .isInstanceOfSatisfying(NghiepVuException.class, e -> {
                    assertThat(e.getMessage()).isEqualTo("Báo cáo đã được xử lý.");
                    assertThat(e.getLoai()).isEqualTo(NghiepVuException.Loai.XUNG_DOT);
                });
        assertThat(ht.db.timBaoCao(bc.getMaBaoCao()).getKhuyenNghi()).isEqualTo("người trước");
    }

    @Test
    void TC08_duyetBaoCaoDaDuyet_baoDaXuLy() {
        BaoCaoHienTruong bc = choXacMinh();
        ht.xacMinh.xacMinh(bc.getMaBaoCao(), maLoai, null, cgA.getMaNguoiDung());
        assertThatThrownBy(() -> ht.xacMinh.xacMinh(bc.getMaBaoCao(), maLoai, null, cgA.getMaNguoiDung()))
                .hasMessage("Báo cáo đã được xử lý.");
    }

    @Test
    void TC09_duyetCaThu5_cungLoaiCungKhuVuc_taoCanhBao() {
        for (int i = 0; i < 4; i++) {
            ht.db.themCaDaXacMinh(nongDan.getMaNguoiDung(), kvA, maLoai, LocalDateTime.now().minusDays(i + 1));
        }
        BaoCaoHienTruong bc = choXacMinh();
        ht.xacMinh.xacMinh(bc.getMaBaoCao(), maLoai, null, cgA.getMaNguoiDung());

        assertThat(ht.db.canhBao).singleElement().satisfies(c -> {
            assertThat(c.getMaDuBao()).isNull();
            assertThat(c.getMaKhuVuc()).isEqualTo(kvA);
        });
        assertThat(ht.db.thongBaoCua(cgA.getMaNguoiDung()))
                .anyMatch(t -> LoaiThongBao.CANH_BAO_DICH.equals(t.getLoai()));
        assertThat(ht.db.thongBaoCua(nongDan.getMaNguoiDung()))
                .anyMatch(t -> LoaiThongBao.CANH_BAO_DICH.equals(t.getLoai()));
    }

    @Test
    void duyetCaThu4_chuaDuNguong_khongTaoCanhBao() {
        for (int i = 0; i < 3; i++) {
            ht.db.themCaDaXacMinh(nongDan.getMaNguoiDung(), kvA, maLoai, LocalDateTime.now().minusDays(i + 1));
        }
        ht.xacMinh.xacMinh(choXacMinh().getMaBaoCao(), maLoai, null, cgA.getMaNguoiDung());
        assertThat(ht.db.canhBao).isEmpty();
    }

    @Test
    void khongDuocXacMinhBaoCaoCuaChinhMinh() {
        BaoCaoHienTruong bc = ht.db.themBaoCao(cgA.getMaNguoiDung(), kvA, TrangThai.CHO_XAC_MINH);
        assertThatThrownBy(() -> ht.xacMinh.xacMinh(bc.getMaBaoCao(), maLoai, null, cgA.getMaNguoiDung()))
                .hasMessageContaining("chính mình");
    }

    @Test
    void tuChoi_baoCaoDaGuiLaiMotLan_dongHoSoLuon() {
        BaoCaoHienTruong bc = choXacMinh();
        bc.setSoLanGuiLai(1);
        ht.xacMinh.tuChoi(bc.getMaBaoCao(), "Ảnh không rõ", cgA.getMaNguoiDung());

        assertThat(ht.db.timBaoCao(bc.getMaBaoCao()).getTrangThai()).isEqualTo(TrangThai.DA_XU_LY);
        assertThat(ht.db.lichSuDal.getByBaoCao(bc.getMaBaoCao())).hasSize(2);
        assertThat(ht.db.thongBaoCua(nongDan.getMaNguoiDung()))
                .anyMatch(t -> LoaiThongBao.DONG_HO_SO.equals(t.getLoai()));
        assertThat(ht.txm.soCommit).isEqualTo(1);
    }

    @Test
    void danhDauDaXuLy_tuDaXacMinh() {
        BaoCaoHienTruong bc = choXacMinh();
        ht.xacMinh.xacMinh(bc.getMaBaoCao(), maLoai, "Phun thuốc theo hướng dẫn", cgA.getMaNguoiDung());
        ht.xacMinh.danhDauDaXuLy(bc.getMaBaoCao(), null, cgA.getMaNguoiDung());
        assertThat(ht.db.timBaoCao(bc.getMaBaoCao()).getTrangThai()).isEqualTo(TrangThai.DA_XU_LY);
    }

    @Test
    void layDanhSachCho_chiKhuVucPhuTrach_vaGanCoQuaHan() {
        BaoCaoHienTruong cu = choXacMinh();
        cu.setNgayVaoTrangThai(LocalDateTime.now().minusHours(50));
        ht.db.themBaoCao(nongDan.getMaNguoiDung(), kvB, TrangThai.CHO_XAC_MINH);

        assertThat(ht.xacMinh.layDanhSachCho(cgA.getMaNguoiDung())).singleElement()
                .satisfies(b -> assertThat(b.isQuaHan()).isTrue());
    }
}

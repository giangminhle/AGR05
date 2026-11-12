package vn.agr05.bll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.agr05.entity.BaoCaoHienTruong;
import vn.agr05.entity.LoaiThongBao;
import vn.agr05.entity.NghiepVuException;
import vn.agr05.entity.NguoiDung;
import vn.agr05.entity.TrangThai;
import vn.agr05.entity.VaiTro;
import vn.agr05.fake.FakeDb;

class BaoCaoServiceTest {

    @TempDir
    Path tam;

    HeThongGia ht;
    NguoiDung nongDan;
    NguoiDung chuyenGia;
    int maKV;

    @BeforeEach
    void chuanBi() {
        ht = new HeThongGia(tam);
        nongDan = ht.db.themNguoiDung("nd", VaiTro.NONG_DAN, "123456");
        chuyenGia = ht.db.themNguoiDung("cg", VaiTro.CHUYEN_GIA, "123456");
        maKV = ht.db.themKhuVuc("Xã A").getMaKhuVuc();
        ht.db.phanCong(chuyenGia.getMaNguoiDung(), maKV);
    }

    BaoCaoHienTruong baoCaoMoi() {
        BaoCaoHienTruong bc = FakeDb.mauBaoCao(maKV);
        bc.setMaNguoiGui(nongDan.getMaNguoiDung());
        return bc;
    }

    long soFileAnh() throws Exception {
        Path d = tam.resolve("anh");
        if (!Files.exists(d)) {
            return 0;
        }
        try (var s = Files.list(d)) {
            return s.count();
        }
    }

    @Test
    void TC01_guiBaoCao_hopLe_luuChoXacMinh_vaBaoChuyenGia() throws Exception {
        int ma = ht.baoCao.guiBaoCao(baoCaoMoi(), List.of(AnhMau.jpg("a.jpg"), AnhMau.jpg("b.jpg"), AnhMau.jpg("c.jpg")));

        BaoCaoHienTruong bc = ht.db.timBaoCao(ma);
        assertThat(bc.getTrangThai()).isEqualTo(TrangThai.CHO_XAC_MINH);
        assertThat(ht.db.hinhAnhDal.getByBaoCao(ma)).hasSize(3);
        assertThat(ht.db.lichSuDal.getByBaoCao(ma)).singleElement()
                .satisfies(l -> assertThat(l.getTrangThaiCu()).isNull());
        assertThat(ht.db.thongBaoCua(chuyenGia.getMaNguoiDung())).singleElement()
                .satisfies(t -> assertThat(t.getLoai()).isEqualTo(LoaiThongBao.BAO_CAO_MOI));
        assertThat(soFileAnh()).isEqualTo(3);
        assertThat(ht.txm.soCommit).isEqualTo(1);
    }

    @Test
    void TC02_guiBaoCao_khongCoAnh_baoLoi_khongLuu() {
        assertThatThrownBy(() -> ht.baoCao.guiBaoCao(baoCaoMoi(), List.of()))
                .isInstanceOf(NghiepVuException.class).hasMessageContaining("ít nhất 1 ảnh");
        assertThat(ht.db.baoCao).isEmpty();
    }

    @Test
    void TC03_guiBaoCao_sauAnh_baoLoi() {
        List<FileTai> sau = IntStream.range(0, 6).mapToObj(i -> AnhMau.jpg(i + ".jpg")).toList();
        assertThatThrownBy(() -> ht.baoCao.guiBaoCao(baoCaoMoi(), sau)).hasMessageContaining("tối đa 5 ảnh");
        assertThat(ht.db.baoCao).isEmpty();
    }

    @Test
    void TC03_guiBaoCao_anh7MB_baoLoi() {
        assertThatThrownBy(() -> ht.baoCao.guiBaoCao(baoCaoMoi(), List.of(AnhMau.jpg("to.jpg", 7L * 1024 * 1024))))
                .hasMessageContaining("5 MB");
        assertThat(ht.db.baoCao).isEmpty();
    }

    @Test
    void TC03_guiBaoCao_viDo95_baoLoi_chiRoO() {
        BaoCaoHienTruong bc = baoCaoMoi();
        bc.setViDo(new BigDecimal("95"));
        assertThatThrownBy(() -> ht.baoCao.guiBaoCao(bc, List.of(AnhMau.jpg("a.jpg"))))
                .isInstanceOfSatisfying(NghiepVuException.class, e -> assertThat(e.getTruong()).isEqualTo("viDo"));
        assertThat(ht.db.baoCao).isEmpty();
    }

    @Test
    void TC03_guiBaoCao_fileDoiDuoi_khongPhaiAnh_baoLoi() {
        FileTai gia = new FileTai("virus.jpg", 100, "MZ...".getBytes());
        assertThatThrownBy(() -> ht.baoCao.guiBaoCao(baoCaoMoi(), List.of(gia))).hasMessageContaining("không phải file");
    }

    @Test
    void TC04_guiBaoCao_matKetNoiKhiGhi_rollback_xoaFileAnh() throws Exception {
        ht.db.loiKhiGhiAnh = true;
        assertThatThrownBy(() -> ht.baoCao.guiBaoCao(baoCaoMoi(), List.of(AnhMau.jpg("a.jpg"), AnhMau.jpg("b.jpg"))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ht.txm.soRollback).isEqualTo(1);
        assertThat(ht.txm.soCommit).isZero();
        assertThat(soFileAnh()).isZero();
    }

    @Test
    void TC10_guiLai_lanThuHai_baoHetLuot_khongLuu() {
        BaoCaoHienTruong bc = ht.db.themBaoCao(nongDan.getMaNguoiDung(), maKV, TrangThai.BI_BAC_BO);
        bc.setSoLanGuiLai(1);
        BaoCaoHienTruong moi = baoCaoMoi();
        moi.setMaBaoCao(bc.getMaBaoCao());
        moi.setTieuDe("Sửa lần 2");

        assertThatThrownBy(() -> ht.baoCao.guiLai(moi, List.of(AnhMau.jpg("x.jpg")), List.of()))
                .hasMessageContaining("hết lượt gửi lại");
        assertThat(ht.db.timBaoCao(bc.getMaBaoCao()).getTieuDe()).isNotEqualTo("Sửa lần 2");
    }

    @Test
    void guiLai_lanDau_chuyenVeChoXacMinh_tangSoLan() {
        BaoCaoHienTruong bc = ht.db.themBaoCao(nongDan.getMaNguoiDung(), maKV, TrangThai.BI_BAC_BO);
        BaoCaoHienTruong moi = baoCaoMoi();
        moi.setMaBaoCao(bc.getMaBaoCao());

        ht.baoCao.guiLai(moi, List.of(AnhMau.jpg("them.jpg")), List.of());

        BaoCaoHienTruong sau = ht.db.timBaoCao(bc.getMaBaoCao());
        assertThat(sau.getTrangThai()).isEqualTo(TrangThai.CHO_XAC_MINH);
        assertThat(sau.getSoLanGuiLai()).isEqualTo(1);
        assertThat(ht.db.hinhAnhDal.getByBaoCao(bc.getMaBaoCao())).hasSize(2);
    }

    @Test
    void layDeSua_baoCaoNguoiKhac_khongDuQuyen() {
        NguoiDung khac = ht.db.themNguoiDung("nd2", VaiTro.NONG_DAN, "123456");
        BaoCaoHienTruong bc = ht.db.themBaoCao(khac.getMaNguoiDung(), maKV, TrangThai.CHO_XAC_MINH);
        assertThatThrownBy(() -> ht.baoCao.layDeSua(bc.getMaBaoCao(), nongDan.getMaNguoiDung()))
                .isInstanceOfSatisfying(NghiepVuException.class,
                        e -> assertThat(e.getLoai()).isEqualTo(NghiepVuException.Loai.KHONG_DU_QUYEN));
    }

    @Test
    void sua_khiChuyenGiaVuaDuyet_baoDaXuLy() {
        BaoCaoHienTruong bc = ht.db.themBaoCao(nongDan.getMaNguoiDung(), maKV, TrangThai.CHO_XAC_MINH);
        BaoCaoHienTruong moi = baoCaoMoi();
        moi.setMaBaoCao(bc.getMaBaoCao());
        // Giả lập chuyên gia duyệt trong lúc nông dân đang sửa: layDeSua thấy Chờ, nhưng UPDATE thì trạng thái đã khác
        IBaoCaoDalChen chen = new IBaoCaoDalChen(ht, bc.getMaBaoCao());
        BaoCaoService sv = new BaoCaoService(chen, ht.db.hinhAnhDal, ht.db.lichSuDal, ht.db.nguoiDungDal,
                ht.db.khuVucDal, ht.thongBao, ht.thamSo, ht.anh, ht.txm.template());
        assertThatThrownBy(() -> sv.guiLai(moi, List.of(), List.of())).hasMessageContaining("đã được xử lý");
        assertThat(ht.txm.soRollback).isEqualTo(1);
    }

    /** BaoCaoDal giả đổi trạng thái ngay trước UPDATE để giả lập tranh chấp. */
    static class IBaoCaoDalChen extends vn.agr05.fake.BaoCaoDalUyQuyen {
        private final HeThongGia ht;
        private final int ma;

        IBaoCaoDalChen(HeThongGia ht, int ma) {
            super(ht.db.baoCaoDal);
            this.ht = ht;
            this.ma = ma;
        }

        @Override
        public boolean updateNoiDung(BaoCaoHienTruong bc, String ttCu) {
            ht.db.timBaoCao(ma).setTrangThai(TrangThai.DA_XAC_MINH);
            return super.updateNoiDung(bc, ttCu);
        }
    }
}

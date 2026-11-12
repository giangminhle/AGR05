package vn.agr05.bll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.agr05.entity.BaoCaoHienTruong;
import vn.agr05.entity.LoaiThongBao;
import vn.agr05.entity.NghiepVuException;
import vn.agr05.entity.NguoiDung;
import vn.agr05.entity.ThamSo;
import vn.agr05.entity.TrangThai;
import vn.agr05.entity.VaiTro;

/** Đăng nhập, quản trị, tham số, thông báo, chức năng nền. */
class QuanTriServiceTest {

    @TempDir
    Path tam;

    HeThongGia ht;
    NguoiDung admin;

    @BeforeEach
    void chuanBi() {
        ht = new HeThongGia(tam);
        admin = ht.db.themNguoiDung("admin", VaiTro.QUAN_TRI, "MatKhau1");
    }

    @Test
    void TC16_nguongNguyCo_0_hoac_150_baoKhongHopLe_khongLuu() {
        assertThatThrownBy(() -> ht.thamSo.capNhat(ThamSo.NGUONG_NGUY_CO, 0)).hasMessageContaining("không hợp lệ");
        assertThatThrownBy(() -> ht.thamSo.capNhat(ThamSo.NGUONG_NGUY_CO, 150)).hasMessageContaining("không hợp lệ");
        assertThat(ht.db.thamSo.get(ThamSo.NGUONG_NGUY_CO)).isEqualTo(70);
        assertThat(ht.thamSo.capNhat(ThamSo.NGUONG_NGUY_CO, 100)).isTrue();
    }

    @Test
    void dangNhap_dung_luuAppSession() {
        NguoiDung nd = ht.account.dangNhap("admin", "MatKhau1");
        assertThat(nd.getMaNguoiDung()).isEqualTo(admin.getMaNguoiDung());
        assertThat(ht.session.layMa()).isEqualTo(admin.getMaNguoiDung());
    }

    @Test
    void dangNhap_saiTen_vaSaiMatKhau_cungMotCau() {
        String loi1 = loi(() -> ht.account.dangNhap("khongco", "MatKhau1"));
        String loi2 = loi(() -> ht.account.dangNhap("admin", "sai"));
        assertThat(loi1).isEqualTo(loi2).isEqualTo(AccountService.LOI_DANG_NHAP);
        assertThat(ht.session.daDangNhap()).isFalse();
    }

    @Test
    void dangNhap_taiKhoanBiKhoa_baoRieng() {
        admin.setBiKhoa(true);
        assertThatThrownBy(() -> ht.account.dangNhap("admin", "MatKhau1")).hasMessageContaining("bị khóa");
    }

    @Test
    void khongKhoaChinhTaiKhoanMinh() {
        ht.account.dangNhap("admin", "MatKhau1");
        assertThatThrownBy(() -> ht.account.khoaTaiKhoan(admin.getMaNguoiDung(), true)).hasMessageContaining("chính tài khoản");
        assertThat(admin.isBiKhoa()).isFalse();
    }

    @Test
    void taoTaiKhoan_bamMatKhau_dangNhapDuoc() {
        NguoiDung nd = new NguoiDung();
        nd.setTenDangNhap("nongdan1");
        nd.setHoTen("Nông dân 1");
        nd.setVaiTro(VaiTro.NONG_DAN);
        ht.account.taoTaiKhoan(nd, "abc123");
        assertThat(nd.getMatKhauHash()).isNotEqualTo("abc123");
        assertThat(ht.account.dangNhap("nongdan1", "abc123").getVaiTro()).isEqualTo(VaiTro.NONG_DAN);
        assertThatThrownBy(() -> ht.account.taoTaiKhoan(nd, "abc123")).hasMessageContaining("đã tồn tại");
    }

    @Test
    void thongBao_khongDanhDauDuocCuaNguoiKhac() {
        NguoiDung khac = ht.db.themNguoiDung("khac", VaiTro.NONG_DAN, "123456");
        int maTB = ht.thongBao.taoThongBao(khac.getMaNguoiDung(), LoaiThongBao.BAO_CAO_MOI, null, "t", "n");
        assertThat(ht.thongBao.danhDauDaDoc(maTB, admin.getMaNguoiDung())).isFalse();
        assertThat(ht.thongBao.demChuaDoc(khac.getMaNguoiDung())).isEqualTo(1);
    }

    @Test
    void chucNangNen_ganCoQuaHan_vaDongHoSoHetHan() {
        NguoiDung nd = ht.db.themNguoiDung("nd", VaiTro.NONG_DAN, "123456");
        int kv = ht.db.themKhuVuc("A").getMaKhuVuc();
        BaoCaoHienTruong cho = ht.db.themBaoCao(nd.getMaNguoiDung(), kv, TrangThai.CHO_XAC_MINH);
        cho.setNgayVaoTrangThai(LocalDateTime.now().minusHours(49));
        BaoCaoHienTruong bacBo = ht.db.themBaoCao(nd.getMaNguoiDung(), kv, TrangThai.BI_BAC_BO);
        bacBo.setNgayVaoTrangThai(LocalDateTime.now().minusDays(8));

        assertThat(ht.hanXuLy.kiemTraQuaHan()).isEqualTo(1);
        assertThat(ht.hanXuLy.kiemTraQuaHan()).isZero();
        assertThat(ht.db.thongBaoCua(admin.getMaNguoiDung())).singleElement()
                .satisfies(t -> assertThat(t.getLoai()).isEqualTo(LoaiThongBao.QUA_HAN));
        assertThat(ht.hanXuLy.dongHoSoHetHan()).isEqualTo(1);
        assertThat(ht.db.timBaoCao(bacBo.getMaBaoCao()).getTrangThai()).isEqualTo(TrangThai.DA_XU_LY);
        assertThat(ht.db.lichSuDal.getByBaoCao(bacBo.getMaBaoCao())).singleElement()
                .satisfies(l -> assertThat(l.getMaNguoiThucHien()).isNull());
    }

    @Test
    void phanCong_trung_baoLoi() {
        NguoiDung cg = ht.db.themNguoiDung("cg", VaiTro.CHUYEN_GIA, "123456");
        int kv = ht.db.themKhuVuc("A").getMaKhuVuc();
        DanhMucService dm = new DanhMucService(ht.db.loaiDal, ht.db.khuVucDal, ht.db.nguoiDungDal);
        assertThat(dm.phanCong(cg.getMaNguoiDung(), kv)).isTrue();
        assertThatThrownBy(() -> dm.phanCong(cg.getMaNguoiDung(), kv)).hasMessageContaining("đã được giao");
        assertThatThrownBy(() -> dm.phanCong(admin.getMaNguoiDung(), kv)).isInstanceOf(NghiepVuException.class);
        assertThat(dm.goPhanCong(cg.getMaNguoiDung(), kv)).isTrue();
        assertThat(dm.conChuyenGia(kv)).isFalse();
    }

    @Test
    void thongKe_chuyenGiaXemKhuVucKhac_khongDuQuyen() {
        NguoiDung cg = ht.db.themNguoiDung("cg", VaiTro.CHUYEN_GIA, "123456");
        int kvA = ht.db.themKhuVuc("A").getMaKhuVuc();
        int kvB = ht.db.themKhuVuc("B").getMaKhuVuc();
        ht.db.phanCong(cg.getMaNguoiDung(), kvA);
        ht.session.datNguoiDung(cg, List.of(kvA));
        ThongKeService tk = new ThongKeService(ht.db.baoCaoDal, ht.db.canhBaoDal, ht.db.duBaoDal, ht.db.nguoiDungDal,
                ht.session);
        assertThat(tk.thongKe(null, null, kvA, null)).isNotNull();
        assertThatThrownBy(() -> tk.thongKe(null, null, kvB, null)).hasMessageContaining("phụ trách");
    }

    private static String loi(Runnable r) {
        try {
            r.run();
            return null;
        } catch (NghiepVuException e) {
            return e.getMessage();
        }
    }
}

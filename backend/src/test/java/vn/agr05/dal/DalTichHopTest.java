package vn.agr05.dal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import vn.agr05.entity.BaoCaoHienTruong;
import vn.agr05.entity.LoaiThongBao;
import vn.agr05.entity.MatKhau;
import vn.agr05.entity.NguoiDung;
import vn.agr05.entity.ThongBao;
import vn.agr05.entity.TrangThai;
import vn.agr05.entity.VaiTro;

/**
 * Test tích hợp với SQL Server thật. Biến môi trường AGR05_TEST_DB = chuỗi JDBC đầy đủ (kèm user, password).
 * Mọi thao tác chạy trong transaction rồi Rollback nên không để lại dữ liệu.
 */
@Tag("integration")
class DalTichHopTest {

    static NamedParameterJdbcTemplate db;
    static TransactionTemplate tx;

    @BeforeAll
    static void ketNoi() {
        String url = System.getenv("AGR05_TEST_DB");
        assumeTrue(url != null && !url.isBlank(), "Chưa đặt AGR05_TEST_DB, bỏ qua test tích hợp");
        DriverManagerDataSource ds = new DriverManagerDataSource(url);
        db = new NamedParameterJdbcTemplate(ds);
        tx = new TransactionTemplate(new DataSourceTransactionManager(ds));
    }

    @Test
    void baoCaoDal_themDoc_doiTrangThaiCoDieuKien() {
        tx.executeWithoutResult(s -> {
            NguoiDungDal ndDal = new NguoiDungDal(db);
            BaoCaoDal bcDal = new BaoCaoDal(db);
            int maND = ndDal.insert(nguoi("it_nd_" + System.nanoTime()));
            int maKV = new KhuVucDal(db).getAll().get(0).getMaKhuVuc();

            BaoCaoHienTruong bc = new BaoCaoHienTruong();
            bc.setTieuDe("Test tích hợp");
            bc.setTenCayTrong("Lúa");
            bc.setMoTa("Mô tả có dấu tiếng Việt");
            bc.setMucDo(2);
            bc.setViDo(new BigDecimal("10.770000"));
            bc.setKinhDo(new BigDecimal("106.700000"));
            bc.setTrangThai(TrangThai.CHO_XAC_MINH);
            bc.setNgayPhatHien(LocalDate.now());
            bc.setMaNguoiGui(maND);
            bc.setMaKhuVuc(maKV);
            int ma = bcDal.insert(bc);

            BaoCaoHienTruong doc = bcDal.getById(ma);
            assertThat(doc.getMoTa()).isEqualTo("Mô tả có dấu tiếng Việt");
            assertThat(doc.getTrangThai()).isEqualTo(TrangThai.CHO_XAC_MINH);
            assertThat(bcDal.getByTrangThai(TrangThai.CHO_XAC_MINH, List.of(maKV))).anyMatch(b -> b.getMaBaoCao() == ma);

            doc.setTrangThai(TrangThai.BI_BAC_BO);
            doc.setNgayVaoTrangThai(java.time.LocalDateTime.now());
            assertThat(bcDal.updateTrangThai(doc, TrangThai.CHO_XAC_MINH)).isTrue();
            // Lần hai: trạng thái cũ không còn là Chờ xác minh → không cập nhật dòng nào (TC08)
            assertThat(bcDal.updateTrangThai(doc, TrangThai.CHO_XAC_MINH)).isFalse();
            s.setRollbackOnly();
        });
    }

    @Test
    void thongBaoDal_demChuaDoc_vaChiNguoiNhanDanhDauDuoc() {
        tx.executeWithoutResult(s -> {
            NguoiDungDal ndDal = new NguoiDungDal(db);
            ThongBaoDal tbDal = new ThongBaoDal(db);
            int a = ndDal.insert(nguoi("it_a_" + System.nanoTime()));
            int b = ndDal.insert(nguoi("it_b_" + System.nanoTime()));
            ThongBao tb = new ThongBao();
            tb.setMaNguoiNhan(a);
            tb.setLoai(LoaiThongBao.BAO_CAO_MOI);
            tb.setTieuDe("Tiêu đề");
            tb.setNoiDung("Nội dung");
            int ma = tbDal.insert(tb);

            assertThat(tbDal.countChuaDoc(a)).isEqualTo(1);
            assertThat(tbDal.markRead(ma, b)).isFalse();
            assertThat(tbDal.markRead(ma, a)).isTrue();
            assertThat(tbDal.countChuaDoc(a)).isZero();
            s.setRollbackOnly();
        });
    }

    private static NguoiDung nguoi(String ten) {
        NguoiDung nd = new NguoiDung();
        nd.setTenDangNhap(ten.substring(0, Math.min(ten.length(), 50)));
        nd.setHoTen("Người test");
        nd.setVaiTro(VaiTro.NONG_DAN);
        nd.setMuoi(MatKhau.taoMuoi());
        nd.setMatKhauHash(MatKhau.bam("123456", nd.getMuoi()));
        return nd;
    }
}

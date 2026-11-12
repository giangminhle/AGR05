package vn.agr05.bll;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import vn.agr05.dal.IBaoCaoDal;
import vn.agr05.dal.IKhuVucDal;
import vn.agr05.dal.ILichSuDal;
import vn.agr05.dal.ILoaiSauBenhDal;
import vn.agr05.dal.INguoiDungDal;
import vn.agr05.entity.BaoCaoHienTruong;
import vn.agr05.entity.KhuVuc;
import vn.agr05.entity.LichSuXacMinh;
import vn.agr05.entity.LoaiSauBenh;
import vn.agr05.entity.MatKhau;
import vn.agr05.entity.NguoiDung;
import vn.agr05.entity.TrangThai;
import vn.agr05.entity.VaiTro;

/**
 * CÔNG CỤ TẠO DỮ LIỆU GIẢ, CHỈ ĐỂ CHẠY THỬ (profile "demo").
 * Sinh tài khoản demo và khoảng 240 ngày báo cáo đã xác minh có các đợt bùng phát giả lập, để thử huấn luyện mô hình AI.
 * Số liệu sinh ra KHÔNG được dùng cho kết quả trong báo cáo. Tiêu đề mọi báo cáo giả bắt đầu bằng "[DỮ LIỆU GIẢ]".
 * Chạy: mvn spring-boot:run -Dspring-boot.run.profiles=demo (chỉ chạy một lần, lần sau thấy tài khoản demo thì bỏ qua).
 */
@Component
@Profile("demo")
@Order(2)
public class DuLieuGiaDemo implements ApplicationRunner {

    static final String MAT_KHAU_DEMO = "Demo@123";
    private static final Logger LOG = LoggerFactory.getLogger(DuLieuGiaDemo.class);
    private static final int SO_NGAY = 240;

    private final INguoiDungDal nguoiDungDal;
    private final IKhuVucDal khuVucDal;
    private final ILoaiSauBenhDal loaiSauBenhDal;
    private final IBaoCaoDal baoCaoDal;
    private final ILichSuDal lichSuDal;
    private final TransactionTemplate tx;

    public DuLieuGiaDemo(INguoiDungDal nguoiDungDal, IKhuVucDal khuVucDal, ILoaiSauBenhDal loaiSauBenhDal,
                         IBaoCaoDal baoCaoDal, ILichSuDal lichSuDal, TransactionTemplate tx) {
        this.nguoiDungDal = nguoiDungDal;
        this.khuVucDal = khuVucDal;
        this.loaiSauBenhDal = loaiSauBenhDal;
        this.baoCaoDal = baoCaoDal;
        this.lichSuDal = lichSuDal;
        this.tx = tx;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (nguoiDungDal.getByTenDangNhap("nongdan_demo") != null) {
            LOG.info("Dữ liệu giả đã có, bỏ qua");
            return;
        }
        List<KhuVuc> dsKV = khuVucDal.getAll();
        List<LoaiSauBenh> dsLoai = loaiSauBenhDal.getAll();
        if (dsKV.isEmpty() || dsLoai.isEmpty()) {
            LOG.warn("Chưa có khu vực hoặc loại sâu bệnh, hãy chạy db/seed_demo.sql trước");
            return;
        }
        int maND = taoTaiKhoan("nongdan_demo", "Nông dân demo", VaiTro.NONG_DAN);
        int maND2 = taoTaiKhoan("nongdan_demo2", "Nông dân demo 2", VaiTro.NONG_DAN);
        int maCG = taoTaiKhoan("chuyengia_demo", "Chuyên gia demo", VaiTro.CHUYEN_GIA);
        taoTaiKhoan("chuyengia_demo2", "Chuyên gia demo 2", VaiTro.CHUYEN_GIA);
        for (KhuVuc kv : dsKV) {
            khuVucDal.insertPhanCong(maCG, kv.getMaKhuVuc());
        }

        Random r = new Random(2025);
        LocalDateTime batDau = LocalDate.now().minusDays(SO_NGAY).atTime(7, 0);
        int dem = 0;
        for (KhuVuc kv : dsKV) {
            for (LoaiSauBenh loai : dsLoai) {
                // Mỗi cặp khu vực - loại có 1 đến 3 đợt bùng phát giả, mỗi đợt kéo dài khoảng 2 tuần
                int soDot = 1 + r.nextInt(3);
                int[] tam = new int[soDot];
                for (int i = 0; i < soDot; i++) {
                    tam[i] = 20 + r.nextInt(SO_NGAY - 30);
                }
                for (int ngay = 0; ngay < SO_NGAY - 1; ngay++) {
                    double lambda = 0.06;
                    for (int t : tam) {
                        lambda += 1.4 * Math.exp(-Math.pow(ngay - t, 2) / (2 * 16.0));
                    }
                    int soCa = poisson(lambda, r);
                    for (int k = 0; k < soCa; k++) {
                        LocalDateTime gui = batDau.plusDays(ngay).plusMinutes(r.nextInt(600));
                        taoBaoCaoGia(r.nextBoolean() ? maND : maND2, maCG, kv, loai, gui, r);
                        dem++;
                    }
                }
            }
        }
        LOG.warn("=== Đã tạo {} báo cáo DỮ LIỆU GIẢ. Tài khoản demo: nongdan_demo, nongdan_demo2, chuyengia_demo, "
                + "chuyengia_demo2, mật khẩu {} ===", dem, MAT_KHAU_DEMO);
    }

    private int taoTaiKhoan(String ten, String hoTen, String vaiTro) {
        NguoiDung nd = new NguoiDung();
        nd.setTenDangNhap(ten);
        nd.setHoTen(hoTen);
        nd.setVaiTro(vaiTro);
        nd.setMuoi(MatKhau.taoMuoi());
        nd.setMatKhauHash(MatKhau.bam(MAT_KHAU_DEMO, nd.getMuoi()));
        return nguoiDungDal.insert(nd);
    }

    /** Một báo cáo giả: gửi → (sau vài giờ) đã xác minh; khoảng 30% sau đó được đánh dấu đã xử lý. Không có ảnh. */
    private void taoBaoCaoGia(int maND, int maCG, KhuVuc kv, LoaiSauBenh loai, LocalDateTime gui, Random r) {
        tx.executeWithoutResult(s -> {
            BaoCaoHienTruong bc = new BaoCaoHienTruong();
            bc.setTieuDe("[DỮ LIỆU GIẢ] " + loai.getTenLoai());
            bc.setTenCayTrong("Lúa");
            bc.setMoTa("Báo cáo sinh tự động để chạy thử mô hình AI.");
            bc.setMucDo(1 + r.nextInt(3));
            bc.setViDo(BigDecimal.valueOf(10.5 + r.nextDouble()).setScale(6, RoundingMode.HALF_UP));
            bc.setKinhDo(BigDecimal.valueOf(106 + r.nextDouble()).setScale(6, RoundingMode.HALF_UP));
            bc.setTrangThai(TrangThai.CHO_XAC_MINH);
            bc.setNgayPhatHien(gui.toLocalDate());
            bc.setNgayGui(gui);
            bc.setMaNguoiGui(maND);
            bc.setMaKhuVuc(kv.getMaKhuVuc());
            int ma = baoCaoDal.insert(bc);
            bc.setMaBaoCao(ma);
            lichSuDal.insert(lichSu(ma, maND, null, TrangThai.CHO_XAC_MINH, "Gửi báo cáo (dữ liệu giả)", gui));

            LocalDateTime duyet = gui.plusHours(2 + r.nextInt(30));
            bc.setTrangThai(TrangThai.DA_XAC_MINH);
            bc.setMaLoai(loai.getMaLoai());
            bc.setNgayVaoTrangThai(duyet);
            baoCaoDal.updateTrangThai(bc, TrangThai.CHO_XAC_MINH);
            lichSuDal.insert(lichSu(ma, maCG, TrangThai.CHO_XAC_MINH, TrangThai.DA_XAC_MINH, "Duyệt (dữ liệu giả)", duyet));

            if (r.nextInt(10) < 3) {
                LocalDateTime xuLy = duyet.plusDays(1 + r.nextInt(3));
                bc.setTrangThai(TrangThai.DA_XU_LY);
                bc.setKhuyenNghi(loai.getBienPhapPhongTru());
                bc.setNgayVaoTrangThai(xuLy);
                baoCaoDal.updateTrangThai(bc, TrangThai.DA_XAC_MINH);
                lichSuDal.insert(lichSu(ma, maCG, TrangThai.DA_XAC_MINH, TrangThai.DA_XU_LY, "Đã xử lý (dữ liệu giả)", xuLy));
            }
        });
    }

    private static LichSuXacMinh lichSu(int ma, Integer nguoi, String cu, String moi, String ghiChu, LocalDateTime tg) {
        LichSuXacMinh ls = BaoCaoService.lichSu(ma, nguoi, cu, moi, ghiChu);
        ls.setThoiGian(tg);
        return ls;
    }

    private static int poisson(double lambda, Random r) {
        double l = Math.exp(-lambda);
        double p = 1;
        int k = 0;
        do {
            k++;
            p *= r.nextDouble();
        } while (p > l);
        return k - 1;
    }
}

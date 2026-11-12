package vn.agr05.bll;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import smile.classification.GradientTreeBoost;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.vector.IntVector;
import vn.agr05.config.CauHinhUngDung;
import vn.agr05.dal.IBaoCaoDal;
import vn.agr05.dal.IDuBaoDal;
import vn.agr05.dal.IMoHinhAIDal;
import vn.agr05.entity.BaoCaoHienTruong;
import vn.agr05.entity.DuBaoNguyCo;
import vn.agr05.entity.MoHinhAI;
import vn.agr05.entity.MucNguyCo;
import vn.agr05.entity.NghiepVuException;
import vn.agr05.entity.ThamSo;

/**
 * Mô-đun AI dự báo nguy cơ dịch (BR08, ARCH05, docs/ai-module.md). Đây là lớp duy nhất gọi thư viện Smile
 * (lớp lồng SmileHocMay bên dưới).
 */
@Service
public class AIService {

    /** Số ngày dự báo và số ngày quan sát để gán nhãn. */
    static final int SO_NGAY_DU_BAO = 7;

    private static final Logger LOG = LoggerFactory.getLogger(AIService.class);

    private final IMoHinhAIDal moHinhAIDal;
    private final IDuBaoDal duBaoDal;
    private final IBaoCaoDal baoCaoDal;
    private final ThamSoService thamSoService;
    private final CanhBaoService canhBaoService;
    private final TransactionTemplate tx;
    private final ThuVienHocMay hocMay;
    private final Path thuMucMoHinh;

    /** Bản đang dùng đã nạp vào bộ nhớ, tránh đọc file mỗi lần dự báo. */
    private Integer maMoHinhDaNap;
    private MoHinhDuBao moHinhDaNap;

    @Autowired
    public AIService(IMoHinhAIDal moHinhAIDal, IDuBaoDal duBaoDal, IBaoCaoDal baoCaoDal, ThamSoService thamSoService,
                     CanhBaoService canhBaoService, TransactionTemplate tx, CauHinhUngDung cauHinh) {
        this(moHinhAIDal, duBaoDal, baoCaoDal, thamSoService, canhBaoService, tx, new SmileHocMay(),
                Paths.get(cauHinh.getThuMucMoHinh()));
    }

    /** Dùng cho test: thay thư viện học máy bằng bản giả. */
    AIService(IMoHinhAIDal moHinhAIDal, IDuBaoDal duBaoDal, IBaoCaoDal baoCaoDal, ThamSoService thamSoService,
              CanhBaoService canhBaoService, TransactionTemplate tx, ThuVienHocMay hocMay, Path thuMucMoHinh) {
        this.moHinhAIDal = moHinhAIDal;
        this.duBaoDal = duBaoDal;
        this.baoCaoDal = baoCaoDal;
        this.thamSoService = thamSoService;
        this.canhBaoService = canhBaoService;
        this.tx = tx;
        this.hocMay = hocMay;
        this.thuMucMoHinh = thuMucMoHinh.toAbsolutePath().normalize();
    }

    /**
     * Mục 3.3.9. Chỉ gọi sau khi transaction duyệt đã Commit. Mọi lỗi được bắt, ghi nhật ký và trả về null (luật 11).
     */
    public DuBaoNguyCo duBaoNguyCo(BaoCaoHienTruong bc) {
        try {
            MoHinhAI mh = moHinhAIDal.getDangSuDung();
            if (mh == null) {
                LOG.info("Chưa có mô hình AI đang dùng, bỏ qua dự báo, chỉ áp dụng BR06");
                return null;
            }
            MoHinhDuBao moHinh = napMoHinh(mh);
            double p = moHinh.xacSuat(taoDacTrung(bc, LocalDateTime.now()));
            int nguong = thamSoService.layGiaTri(ThamSo.NGUONG_NGUY_CO);

            DuBaoNguyCo d = new DuBaoNguyCo();
            d.setMaBaoCao(bc.getMaBaoCao());
            d.setMaMoHinh(mh.getMaMoHinh());
            d.setXacSuat(Math.max(0, Math.min(1, p)));
            d.setMucNguyCo(xepMuc(d.getXacSuat(), nguong / 100.0));
            d.setSoNgayDuBao(SO_NGAY_DU_BAO);
            d.setNgayDuBao(LocalDateTime.now());
            tx.executeWithoutResult(s -> {
                d.setMaDuBao(duBaoDal.insert(d));
                if (d.laNguyCoCao()) {
                    canhBaoService.taoCanhBaoTuDuBao(d, bc);
                }
            });
            return d;
        } catch (Exception e) {
            LOG.error("Dự báo nguy cơ lỗi cho báo cáo {}, chỉ áp dụng BR06", bc.getMaBaoCao(), e);
            return null;
        }
    }

    /**
     * Mục 3.3.10 (UC19): dựng bảng mẫu từ báo cáo Đã xác minh, Đã xử lý; chia theo thời gian 80/20; huấn luyện,
     * đánh giá, lưu file và ghi một dòng MoHinhAI với DangSuDung = 0.
     */
    public MoHinhAI huanLuyenLai() {
        int nguongCanhBao = thamSoService.layGiaTri(ThamSo.NGUONG_CANH_BAO);
        LocalDateTime bayGio = LocalDateTime.now();
        List<BaoCaoHienTruong> dsMau = new ArrayList<>();
        for (BaoCaoHienTruong bc : baoCaoDal.getDaXacMinh()) {
            // Mẫu chưa đủ 7 ngày quan sát thì chưa biết nhãn, bỏ ra
            if (bc.getNgayXacMinh() != null && !bc.getNgayXacMinh().plusDays(SO_NGAY_DU_BAO).isAfter(bayGio)) {
                dsMau.add(bc);
            }
        }
        dsMau.sort(Comparator.comparing(BaoCaoHienTruong::getNgayXacMinh));
        int n = dsMau.size();
        List<DacTrung> x = new ArrayList<>(n);
        int[] y = new int[n];
        for (int i = 0; i < n; i++) {
            BaoCaoHienTruong bc = dsMau.get(i);
            x.add(taoDacTrung(bc, bc.getNgayXacMinh()));
            int soCaSau = baoCaoDal.demCaDaXacMinh(bc.getMaKhuVuc(), bc.getMaLoai(), bc.getNgayXacMinh().plusSeconds(1),
                    bc.getNgayXacMinh().plusDays(SO_NGAY_DU_BAO).plusSeconds(1));
            y[i] = soCaSau >= nguongCanhBao ? 1 : 0;
        }
        int cat = (int) Math.floor(n * 0.8);
        if (!coDuHaiNhan(y, 0, n) || !coDuHaiNhan(y, 0, cat) || n - cat < 1) {
            throw NghiepVuException.khongHopLe(
                    "Chưa đủ dữ liệu để huấn luyện: cần báo cáo đã xác minh có cả mẫu bùng phát và không bùng phát.", null);
        }

        List<Integer> dsLoai = x.stream().map(DacTrung::maLoai).distinct().sorted().toList();
        List<Integer> dsKV = x.stream().map(DacTrung::maKhuVuc).distinct().sorted().toList();
        MoHinhDuBao moHinh;
        try {
            moHinh = hocMay.huanLuyen(x.subList(0, cat), java.util.Arrays.copyOfRange(y, 0, cat), dsLoai, dsKV);
        } catch (RuntimeException e) {
            LOG.error("Huấn luyện mô hình lỗi", e);
            throw NghiepVuException.khongHopLe("Huấn luyện lỗi, giữ phiên bản cũ: " + e.getMessage(), null);
        }

        ChiSoDanhGia cs = danhGia(moHinh, x.subList(cat, n), java.util.Arrays.copyOfRange(y, cat, n));
        String phienBan = "v" + bayGio.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String tenFile = phienBan + ".model";
        Path file = thuMucMoHinh.resolve(tenFile);
        try {
            Files.createDirectories(thuMucMoHinh);
            hocMay.luu(moHinh, file);
            MoHinhAI mh = new MoHinhAI();
            mh.setPhienBan(phienBan);
            mh.setDuongDanFile(tenFile);
            mh.setNgayHuanLuyen(bayGio);
            mh.setSoMauHuanLuyen(cat);
            mh.setAccuracy(cs.accuracy());
            mh.setF1(cs.f1());
            mh.setAuc(cs.auc());
            mh.setDangSuDung(false);
            mh.setMaMoHinh(moHinhAIDal.insert(mh));
            LOG.info("Huấn luyện xong {}: {} mẫu huấn luyện, {} mẫu kiểm tra, Accuracy={}, Precision={}, Recall={}, F1={}, AUC={}",
                    phienBan, cat, n - cat, cs.accuracy(), cs.precision(), cs.recall(), cs.f1(), cs.auc());
            return mh;
        } catch (IOException | RuntimeException e) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
                // file chưa kịp tạo
            }
            LOG.error("Lưu mô hình lỗi", e);
            throw NghiepVuException.khongHopLe("Lưu mô hình lỗi, giữ phiên bản cũ.", null);
        }
    }

    /** ARCH05: chỉ kích hoạt khi F1 không thấp hơn bản đang dùng (TC15). */
    public boolean kichHoat(int maMoHinh) {
        MoHinhAI mh = moHinhAIDal.getById(maMoHinh);
        if (mh == null) {
            throw NghiepVuException.khongTimThay("Không tìm thấy phiên bản mô hình.");
        }
        MoHinhAI dangDung = moHinhAIDal.getDangSuDung();
        if (dangDung != null && dangDung.getMaMoHinh().equals(mh.getMaMoHinh())) {
            return true;
        }
        if (!mh.khongKemHon(dangDung)) {
            throw NghiepVuException.khongHopLe("Phiên bản " + mh.getPhienBan() + " có F1 thấp hơn bản đang dùng "
                    + dangDung.getPhienBan() + ", không kích hoạt.", null);
        }
        MoHinhDuBao moHinh = hocMay.nap(thuMucMoHinh.resolve(mh.getDuongDanFile()));
        tx.executeWithoutResult(s -> moHinhAIDal.datDangSuDung(maMoHinh));
        synchronized (this) {
            maMoHinhDaNap = maMoHinh;
            moHinhDaNap = moHinh;
        }
        return true;
    }

    public List<MoHinhAI> layDanhSach() {
        return moHinhAIDal.getAll();
    }

    public MoHinhAI layDangSuDung() {
        return moHinhAIDal.getDangSuDung();
    }

    public DuBaoNguyCo layDuBaoCuaBaoCao(int maBaoCao) {
        return duBaoDal.getByBaoCao(maBaoCao);
    }

    /** Đặc trưng tính tại thời điểm t: số ca 7, 14, 30 ngày trước, xu hướng, mức độ trung bình 14 ngày, tháng. */
    DacTrung taoDacTrung(BaoCaoHienTruong bc, LocalDateTime t) {
        int kv = bc.getMaKhuVuc();
        int loai = bc.getMaLoai();
        LocalDateTime den = t.plusSeconds(1);
        int soCa7 = baoCaoDal.demCaDaXacMinh(kv, loai, t.minusDays(7), den);
        int soCa14 = baoCaoDal.demCaDaXacMinh(kv, loai, t.minusDays(14), den);
        int soCa30 = baoCaoDal.demCaDaXacMinh(kv, loai, t.minusDays(30), den);
        int soCa7TruocDo = soCa14 - soCa7;
        double mucDo = baoCaoDal.mucDoTrungBinh(kv, loai, t.minusDays(14), den);
        return new DacTrung(soCa7, soCa14, soCa30, soCa7 - soCa7TruocDo, mucDo, t.getMonthValue(), loai, kv);
    }

    /** CAO khi xác suất ≥ ngưỡng; TRUNG_BINH khi ≥ nửa ngưỡng; còn lại THAP. */
    String xepMuc(double xacSuat, double nguong) {
        if (xacSuat >= nguong) {
            return MucNguyCo.CAO;
        }
        if (xacSuat >= nguong / 2) {
            return MucNguyCo.TRUNG_BINH;
        }
        return MucNguyCo.THAP;
    }

    private synchronized MoHinhDuBao napMoHinh(MoHinhAI mh) {
        if (!Objects.equals(maMoHinhDaNap, mh.getMaMoHinh()) || moHinhDaNap == null) {
            moHinhDaNap = hocMay.nap(thuMucMoHinh.resolve(mh.getDuongDanFile()));
            maMoHinhDaNap = mh.getMaMoHinh();
        }
        return moHinhDaNap;
    }

    private static boolean coDuHaiNhan(int[] y, int tu, int den) {
        boolean co0 = false;
        boolean co1 = false;
        for (int i = tu; i < den; i++) {
            co0 |= y[i] == 0;
            co1 |= y[i] == 1;
        }
        return co0 && co1;
    }

    record ChiSoDanhGia(double accuracy, double precision, double recall, double f1, double auc) {
    }

    /** Accuracy, Precision, Recall, F1 (ngưỡng 0.5) và AUC trên tập kiểm tra. */
    static ChiSoDanhGia danhGia(MoHinhDuBao moHinh, List<DacTrung> x, int[] y) {
        int n = y.length;
        double[] p = new double[n];
        int tp = 0;
        int fp = 0;
        int tn = 0;
        int fn = 0;
        for (int i = 0; i < n; i++) {
            p[i] = moHinh.xacSuat(x.get(i));
            boolean du = p[i] >= 0.5;
            if (du && y[i] == 1) {
                tp++;
            } else if (du) {
                fp++;
            } else if (y[i] == 0) {
                tn++;
            } else {
                fn++;
            }
        }
        double acc = n == 0 ? 0 : (double) (tp + tn) / n;
        double prec = tp + fp == 0 ? 0 : (double) tp / (tp + fp);
        double rec = tp + fn == 0 ? 0 : (double) tp / (tp + fn);
        double f1 = prec + rec == 0 ? 0 : 2 * prec * rec / (prec + rec);
        return new ChiSoDanhGia(acc, prec, rec, f1, auc(p, y));
    }

    /** AUC theo cách đếm cặp (dương, âm); tập kiểm tra chỉ có một nhãn thì trả 0.5. */
    static double auc(double[] p, int[] y) {
        long cap = 0;
        double dung = 0;
        for (int i = 0; i < y.length; i++) {
            if (y[i] != 1) {
                continue;
            }
            for (int j = 0; j < y.length; j++) {
                if (y[j] == 0) {
                    cap++;
                    dung += p[i] > p[j] ? 1 : (p[i] == p[j] ? 0.5 : 0);
                }
            }
        }
        return cap == 0 ? 0.5 : dung / cap;
    }

    /** Ranh giới với thư viện học máy, để test thay được bằng bản giả. */
    interface ThuVienHocMay {

        MoHinhDuBao huanLuyen(List<DacTrung> x, int[] y, List<Integer> dsLoai, List<Integer> dsKhuVuc);

        void luu(MoHinhDuBao moHinh, Path file);

        MoHinhDuBao nap(Path file);
    }

    /** Cài đặt bằng Smile GradientTreeBoost. */
    static final class SmileHocMay implements ThuVienHocMay {

        @Override
        public MoHinhDuBao huanLuyen(List<DacTrung> x, int[] y, List<Integer> dsLoai, List<Integer> dsKhuVuc) {
            String[] ten = tenCot(dsLoai, dsKhuVuc);
            double[][] mt = new double[x.size()][];
            for (int i = 0; i < x.size(); i++) {
                mt[i] = maHoa(x.get(i), dsLoai, dsKhuVuc);
            }
            DataFrame df = DataFrame.of(mt, ten).merge(IntVector.of("nhan", y));
            int nodeSize = Math.max(2, Math.min(5, x.size() / 10));
            GradientTreeBoost gbt = GradientTreeBoost.fit(Formula.lhs("nhan"), df, 200, 20, 4, nodeSize, 0.05, 0.7);
            return new MoHinhSmile(gbt, ten, dsLoai, dsKhuVuc);
        }

        @Override
        public void luu(MoHinhDuBao moHinh, Path file) {
            try (OutputStream os = Files.newOutputStream(file); ObjectOutputStream out = new ObjectOutputStream(os)) {
                out.writeObject(moHinh);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public MoHinhDuBao nap(Path file) {
            try (InputStream is = Files.newInputStream(file); ObjectInputStream in = new ObjectInputStream(is)) {
                return (MoHinhDuBao) in.readObject();
            } catch (IOException e) {
                throw new UncheckedIOException("Không nạp được file mô hình " + file, e);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("File mô hình không hợp lệ", e);
            }
        }

        static String[] tenCot(List<Integer> dsLoai, List<Integer> dsKhuVuc) {
            List<String> ten = new ArrayList<>(List.of("soCa7", "soCa14", "soCa30", "xuHuong", "mucDoTrungBinh"));
            for (int t = 1; t <= 12; t++) {
                ten.add("thang_" + t);
            }
            dsLoai.forEach(l -> ten.add("loai_" + l));
            dsKhuVuc.forEach(k -> ten.add("kv_" + k));
            return ten.toArray(String[]::new);
        }

        /** Số liệu giữ nguyên, tháng, loại, khu vực mã hóa one-hot. Loại hoặc khu vực mới (chưa có khi huấn luyện) toàn 0. */
        static double[] maHoa(DacTrung d, List<Integer> dsLoai, List<Integer> dsKhuVuc) {
            double[] v = new double[5 + 12 + dsLoai.size() + dsKhuVuc.size()];
            v[0] = d.soCa7();
            v[1] = d.soCa14();
            v[2] = d.soCa30();
            v[3] = d.xuHuong();
            v[4] = d.mucDoTrungBinh();
            v[4 + d.thang()] = 1;
            int iLoai = dsLoai.indexOf(d.maLoai());
            if (iLoai >= 0) {
                v[17 + iLoai] = 1;
            }
            int iKV = dsKhuVuc.indexOf(d.maKhuVuc());
            if (iKV >= 0) {
                v[17 + dsLoai.size() + iKV] = 1;
            }
            return v;
        }
    }

    /** Mô hình Smile kèm thông tin mã hóa, lưu xuống file bằng Java serialization. */
    static final class MoHinhSmile implements MoHinhDuBao {

        private static final long serialVersionUID = 1L;

        private final GradientTreeBoost gbt;
        private final String[] ten;
        private final ArrayList<Integer> dsLoai;
        private final ArrayList<Integer> dsKhuVuc;

        MoHinhSmile(GradientTreeBoost gbt, String[] ten, List<Integer> dsLoai, List<Integer> dsKhuVuc) {
            this.gbt = gbt;
            this.ten = ten;
            this.dsLoai = new ArrayList<>(dsLoai);
            this.dsKhuVuc = new ArrayList<>(dsKhuVuc);
        }

        @Override
        public double xacSuat(DacTrung x) {
            double[] v = SmileHocMay.maHoa(x, dsLoai, dsKhuVuc);
            double[] hauNghiem = new double[2];
            gbt.predict(DataFrame.of(new double[][]{v}, ten).get(0), hauNghiem);
            return hauNghiem[1];
        }

        @Override
        public List<Integer> dsLoai() {
            return dsLoai;
        }

        @Override
        public List<Integer> dsKhuVuc() {
            return dsKhuVuc;
        }
    }
}

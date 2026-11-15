package vn.agr05.bll;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import vn.agr05.dal.IBaoCaoDal;
import vn.agr05.dal.IHinhAnhDal;
import vn.agr05.dal.IKhuVucDal;
import vn.agr05.dal.ILichSuDal;
import vn.agr05.dal.ILoaiSauBenhDal;
import vn.agr05.dal.INguoiDungDal;
import vn.agr05.entity.BaoCaoHienTruong;
import vn.agr05.entity.HinhAnh;
import vn.agr05.entity.KhuVuc;
import vn.agr05.entity.LichSuXacMinh;
import vn.agr05.entity.LoaiSauBenh;
import vn.agr05.entity.LoaiThongBao;
import vn.agr05.entity.MatKhau;
import vn.agr05.entity.MoHinhAI;
import vn.agr05.entity.NguoiDung;
import vn.agr05.entity.TrangThai;
import vn.agr05.entity.VaiTro;

/**
 * DỮ LIỆU MẪU ĐỂ CHẠY THỬ VÀ DEMO (profile "demo"). Chạy một lần; đã có tài khoản qt.minhkhoa thì bỏ qua.
 * <ul>
 *   <li>7 tài khoản (1 quản trị, 2 chuyên gia, 4 nông dân), 4 khu vực ở huyện Thới Lai (Cần Thơ).</li>
 *   <li>Khoảng 8 tháng báo cáo đã xác minh có các đợt bùng phát MÔ PHỎNG để huấn luyện mô hình AI. Số liệu này sinh
 *       ngẫu nhiên, không phải số liệu điều tra thật, không dùng làm kết quả trong báo cáo đồ án. Nhận biết: là các
 *       báo cáo gửi trước ngày chạy công cụ này hơn 7 ngày.</li>
 *   <li>Tình huống hiện tại ở "Xã Thới Thạnh - Cánh đồng lớn": 4 ca Rầy nâu đã xác minh trong 4 ngày qua (duyệt thêm
 *       1 ca là đủ ngưỡng BR06), 3 báo cáo chờ xác minh (1 báo cáo quá 48 giờ), 1 báo cáo bị bác bỏ còn được gửi lại.</li>
 * </ul>
 * Ảnh là ảnh thật từ Wikimedia Commons (resources/anh-mau, nguồn và giấy phép trong NGUON.md).
 */
@Component
@Profile("demo")
@Order(2)
public class DuLieuMau implements ApplicationRunner {

    static final String MAT_KHAU = "Agr05@2026";
    static final String KHU_VUC_CHINH = "Xã Thới Thạnh - Cánh đồng lớn";
    private static final Logger LOG = LoggerFactory.getLogger(DuLieuMau.class);
    private static final int SO_NGAY = 240;
    /** Lịch sử dừng trước hôm nay 8 ngày để tuần gần nhất chỉ có tình huống demo (BR06 xét 7 ngày). */
    private static final int NGAY_KET_THUC_LICH_SU = 8;

    /** Tên khu vực → {vĩ độ, kinh độ} tâm cánh đồng, mô tả. */
    private static final Object[][] KHU_VUC = {
        {KHU_VUC_CHINH, 10.0650, 105.5870, "Huyện Thới Lai, TP Cần Thơ. Lúa 3 vụ, khoảng 150 ha"},
        {"Xã Định Môn - Cánh đồng mẫu lớn", 10.0950, 105.5760, "Huyện Thới Lai, TP Cần Thơ. Liên kết tiêu thụ, khoảng 200 ha"},
        {"Xã Trường Xuân - Vùng lúa chất lượng cao", 10.0480, 105.4920, "Huyện Thới Lai, TP Cần Thơ. Giống ST25, OM18, khoảng 120 ha"},
        {"Xã Xuân Thắng - Ruộng ven kênh", 10.0300, 105.5600, "Huyện Thới Lai, TP Cần Thơ. Ruộng nhỏ ven kênh, khoảng 80 ha"},
    };

    /** Nội dung báo cáo theo loại: tiêu đề, mô tả (có %d là mật độ hoặc tỉ lệ), ảnh. */
    private static final Map<String, NoiDung> NOI_DUNG = new LinkedHashMap<>();

    static {
        NOI_DUNG.put("Rầy nâu", new NoiDung(
                new String[]{"Rầy nâu bám dày gốc lúa", "Rầy cám nở rộ sau mưa", "Lúa vàng từng chòm, nghi cháy rầy",
                    "Mật độ rầy nâu tăng nhanh"},
                "Vạch gốc lúa thấy rầy nâu và rầy cám bám dày, ước khoảng %d con/khóm. Một số chòm lúa đã vàng, bẹ lá có vết thâm.",
                new String[]{"ray-nau-1.jpg", "ray-nau-2.jpg", "ray-nau-3.jpg"}));
        NOI_DUNG.put("Bệnh đạo ôn", new NoiDung(
                new String[]{"Lá lúa có vết hình thoi, tâm xám", "Nghi đạo ôn lá trên ruộng sạ dày", "Đốt thân, cổ bông thâm đen"},
                "Trên lá có nhiều vết bệnh hình thoi, tâm xám trắng, viền nâu đỏ; khoảng %d%% số lá bị bệnh. Mấy hôm nay trời âm u, sương nhiều.",
                new String[]{"dao-on-1.jpg", "dao-on-2.jpg"}));
        NOI_DUNG.put("Sâu cuốn lá nhỏ", new NoiDung(
                new String[]{"Lá lúa bị cuốn thành ống", "Nhiều bướm nhỏ bay khi lội ruộng", "Lá có vệt trắng dọc gân"},
                "Nhiều lá bị cuốn dọc thành ống, bên trong có sâu non màu xanh vàng; ước khoảng %d sâu/m². Lá bị ăn để lại vệt trắng.",
                new String[]{"cuon-la-1.jpg", "cuon-la-2.jpg"}));
        NOI_DUNG.put("Bệnh bạc lá", new NoiDung(
                new String[]{"Mép lá cháy vàng sau mưa giông", "Lá khô trắng từ chóp xuống", "Nghi bạc lá lan theo bờ"},
                "Vết bệnh chạy dọc mép lá từ chóp xuống, lúc đầu vàng sau chuyển trắng xám; khoảng %d%% diện tích có triệu chứng.",
                new String[]{"bac-la-1.jpg"}));
        NOI_DUNG.put("Sâu đục thân", new NoiDung(
                new String[]{"Dảnh héo rải rác trong ruộng", "Bông bạc sau trổ", "Có ổ trứng bướm trên lá"},
                "Ruộng có dảnh héo, rút nhẹ thì dảnh bị đứt, trong thân có sâu non; tỉ lệ dảnh héo ước khoảng %d%%.",
                new String[]{"duc-than-1.jpg"}));
    }

    private static final String[] GIAI_DOAN = {"đẻ nhánh", "đứng cái", "làm đòng", "trổ", "ngậm sữa"};

    private record NoiDung(String[] tieuDe, String moTa, String[] anh) {
    }

    private final INguoiDungDal nguoiDungDal;
    private final IKhuVucDal khuVucDal;
    private final ILoaiSauBenhDal loaiSauBenhDal;
    private final IBaoCaoDal baoCaoDal;
    private final IHinhAnhDal hinhAnhDal;
    private final ILichSuDal lichSuDal;
    private final ThongBaoService thongBaoService;
    private final AnhStorage anhStorage;
    private final AIService aiService;
    private final TransactionTemplate tx;
    private final Random r = new Random(2026);
    /** Tên file ảnh mẫu → đường dẫn đã lưu (mỗi ảnh lưu một lần, nhiều báo cáo lịch sử dùng chung). */
    private final Map<String, String> anhDaLuu = new HashMap<>();

    public DuLieuMau(INguoiDungDal nguoiDungDal, IKhuVucDal khuVucDal, ILoaiSauBenhDal loaiSauBenhDal,
                     IBaoCaoDal baoCaoDal, IHinhAnhDal hinhAnhDal, ILichSuDal lichSuDal,
                     ThongBaoService thongBaoService, AnhStorage anhStorage, AIService aiService, TransactionTemplate tx) {
        this.nguoiDungDal = nguoiDungDal;
        this.khuVucDal = khuVucDal;
        this.loaiSauBenhDal = loaiSauBenhDal;
        this.baoCaoDal = baoCaoDal;
        this.hinhAnhDal = hinhAnhDal;
        this.lichSuDal = lichSuDal;
        this.thongBaoService = thongBaoService;
        this.anhStorage = anhStorage;
        this.aiService = aiService;
        this.tx = tx;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (nguoiDungDal.getByTenDangNhap("qt.minhkhoa") != null) {
            LOG.info("Dữ liệu mẫu đã có, bỏ qua");
            return;
        }
        List<LoaiSauBenh> dsLoai = loaiSauBenhDal.getAll();
        if (dsLoai.isEmpty()) {
            LOG.warn("Chưa có loại sâu bệnh, hãy chạy db/seed_demo.sql trước");
            return;
        }

        taoTaiKhoan("qt.minhkhoa", "Đặng Minh Khoa", VaiTro.QUAN_TRI, "0939112233");
        int cgTam = taoTaiKhoan("cg.thanhtam", "Võ Thanh Tâm", VaiTro.CHUYEN_GIA, "0918456123");
        int cgAnh = taoTaiKhoan("cg.ngocanh", "Huỳnh Ngọc Ánh", VaiTro.CHUYEN_GIA, "0907345678");
        int ndBay = taoTaiKhoan("nd.vanbay", "Trần Văn Bảy", VaiTro.NONG_DAN, "0945201876");
        int ndMai = taoTaiKhoan("nd.thimai", "Lê Thị Mai", VaiTro.NONG_DAN, "0976332415");
        int ndTu = taoTaiKhoan("nd.vantu", "Nguyễn Văn Tư", VaiTro.NONG_DAN, "0362118790");
        int ndUt = taoTaiKhoan("nd.vanut", "Phạm Văn Út", VaiTro.NONG_DAN, "0388654021");
        int[] nongDan = {ndBay, ndMai, ndTu, ndUt};

        // Khu vực: dùng lại nếu đã có (db/seed_demo.sql hoặc script làm mới đã đặt tên), chưa có thì tạo
        Map<String, KhuVuc> theoTen = new HashMap<>();
        khuVucDal.getAll().forEach(k -> theoTen.put(k.getTenKhuVuc(), k));
        List<KhuVuc> dsKV = new ArrayList<>();
        Map<Integer, double[]> toaDo = new HashMap<>();
        for (Object[] kvMau : KHU_VUC) {
            KhuVuc kv = theoTen.get((String) kvMau[0]);
            if (kv == null) {
                kv = new KhuVuc();
                kv.setTenKhuVuc((String) kvMau[0]);
                kv.setMoTa((String) kvMau[3]);
                kv.setMaKhuVuc(khuVucDal.insert(kv));
            }
            dsKV.add(kv);
            toaDo.put(kv.getMaKhuVuc(), new double[]{(Double) kvMau[1], (Double) kvMau[2]});
        }
        Map<Integer, Integer> chuyenGiaCua = new HashMap<>();
        for (int i = 0; i < dsKV.size(); i++) {
            int cg = i < 2 ? cgTam : cgAnh;
            int maKV = dsKV.get(i).getMaKhuVuc();
            if (!khuVucDal.coPhanCong(cg, maKV)) {
                khuVucDal.insertPhanCong(cg, maKV);
            }
            chuyenGiaCua.put(maKV, cg);
        }

        int soLichSu = taoLichSu(dsKV, dsLoai, nongDan, chuyenGiaCua, toaDo);
        taoTinhHuongHienTai(dsKV.get(0), toaDo.get(dsKV.get(0).getMaKhuVuc()), dsLoai, cgTam, ndBay, ndMai, ndTu);
        huanLuyenMoHinhDau();

        LOG.warn("=== Đã tạo dữ liệu mẫu: {} báo cáo lịch sử (MÔ PHỎNG) và tình huống hiện tại ở \"{}\". "
                + "Tài khoản: qt.minhkhoa, cg.thanhtam, cg.ngocanh, nd.vanbay, nd.thimai, nd.vantu, nd.vanut; mật khẩu {} ===",
                soLichSu, KHU_VUC_CHINH, MAT_KHAU);
    }

    // ---------------------------------------------------------------- lịch sử để huấn luyện AI

    private int taoLichSu(List<KhuVuc> dsKV, List<LoaiSauBenh> dsLoai, int[] nongDan, Map<Integer, Integer> chuyenGiaCua,
                          Map<Integer, double[]> toaDo) {
        LocalDateTime batDau = LocalDate.now().minusDays(SO_NGAY).atTime(6, 0);
        int dem = 0;
        for (KhuVuc kv : dsKV) {
            for (LoaiSauBenh loai : dsLoai) {
                // Mỗi cặp khu vực - loại có 1 đến 3 đợt bùng phát, mỗi đợt kéo dài khoảng 2 tuần
                int soDot = 1 + r.nextInt(3);
                int[] tam = new int[soDot];
                for (int i = 0; i < soDot; i++) {
                    tam[i] = 20 + r.nextInt(SO_NGAY - 40);
                }
                for (int ngay = 0; ngay <= SO_NGAY - NGAY_KET_THUC_LICH_SU; ngay++) {
                    double lambda = 0.06;
                    for (int t : tam) {
                        lambda += 1.4 * Math.exp(-Math.pow(ngay - t, 2) / (2 * 16.0));
                    }
                    int soCa = poisson(lambda);
                    for (int k = 0; k < soCa; k++) {
                        LocalDateTime gui = batDau.plusDays(ngay).plusMinutes(r.nextInt(660));
                        int nguoiGui = nongDan[r.nextInt(nongDan.length)];
                        taoBaoCaoLichSu(nguoiGui, chuyenGiaCua.get(kv.getMaKhuVuc()), kv, loai, gui, toaDo.get(kv.getMaKhuVuc()));
                        dem++;
                    }
                }
            }
        }
        return dem;
    }

    /** Một báo cáo lịch sử: gửi → vài giờ sau được xác minh; khoảng 30% sau đó được đánh dấu đã xử lý. */
    private void taoBaoCaoLichSu(int maND, int maCG, KhuVuc kv, LoaiSauBenh loai, LocalDateTime gui, double[] tam) {
        NoiDung nd = noiDung(loai);
        BaoCaoHienTruong bc = baoCaoMoi(maND, kv.getMaKhuVuc(), chon(nd.tieuDe()), moTa(nd), 1 + r.nextInt(3), gui, tam);
        String anh = chon(nd.anh());
        tx.executeWithoutResult(s -> {
            int ma = baoCaoDal.insert(bc);
            bc.setMaBaoCao(ma);
            themAnh(ma, anh);
            lichSuDal.insert(lichSu(ma, maND, null, TrangThai.CHO_XAC_MINH, "Gửi báo cáo", gui));

            LocalDateTime duyet = gui.plusHours(2 + r.nextInt(30));
            bc.setTrangThai(TrangThai.DA_XAC_MINH);
            bc.setMaLoai(loai.getMaLoai());
            bc.setKhuyenNghi(loai.getBienPhapPhongTru());
            bc.setNgayVaoTrangThai(duyet);
            baoCaoDal.updateTrangThai(bc, TrangThai.CHO_XAC_MINH);
            lichSuDal.insert(lichSu(ma, maCG, TrangThai.CHO_XAC_MINH, TrangThai.DA_XAC_MINH,
                    "Xác nhận " + loai.getTenLoai().toLowerCase(), duyet));

            if (r.nextInt(10) < 3) {
                LocalDateTime xuLy = duyet.plusDays(1 + r.nextInt(3));
                bc.setTrangThai(TrangThai.DA_XU_LY);
                bc.setNgayVaoTrangThai(xuLy);
                baoCaoDal.updateTrangThai(bc, TrangThai.DA_XAC_MINH);
                lichSuDal.insert(lichSu(ma, maCG, TrangThai.DA_XAC_MINH, TrangThai.DA_XU_LY,
                        "Nông dân đã phun xử lý theo khuyến nghị", xuLy));
            }
        });
    }

    // ---------------------------------------------------------------- tình huống hiện tại để demo

    private void taoTinhHuongHienTai(KhuVuc kv, double[] tam, List<LoaiSauBenh> dsLoai, int maCG, int ndBay, int ndMai,
                                     int ndTu) {
        LoaiSauBenh rayNau = timLoai(dsLoai, "Rầy nâu");
        LocalDateTime bayGio = LocalDateTime.now();
        int maKV = kv.getMaKhuVuc();

        // 4 ca Rầy nâu đã xác minh trong 4 ngày qua: còn thiếu 1 ca nữa là đủ ngưỡng BR06
        String[][] daXacMinh = {
            {"Rầy nâu xuất hiện nhiều ở bờ ruộng phía kênh", "ray-nau-2.jpg"},
            {"Rầy cám nở rộ sau mưa", "ray-nau-3.jpg"},
            {"Mật độ rầy nâu tăng nhanh", "ray-nau-2.jpg"},
            {"Rầy nâu bám gốc lúa giai đoạn đòng", "ray-nau-3.jpg"},
        };
        for (int i = 0; i < daXacMinh.length; i++) {
            LocalDateTime gui = bayGio.minusDays(i + 1).minusHours(3);
            int nguoiGui = i % 2 == 0 ? ndTu : ndMai;
            int ma = taoBaoCaoCho(nguoiGui, maKV, daXacMinh[i][0],
                    "Vạch gốc lúa thấy rầy nâu bám khoảng " + (2500 + r.nextInt(2000)) + " con/m², lúa đang làm đòng.",
                    2, gui, tam, daXacMinh[i][1]);
            BaoCaoHienTruong bc = baoCaoDal.getById(ma);
            bc.setTrangThai(TrangThai.DA_XAC_MINH);
            bc.setMaLoai(rayNau.getMaLoai());
            bc.setKhuyenNghi(rayNau.getBienPhapPhongTru());
            bc.setNgayVaoTrangThai(gui.plusHours(2));
            tx.executeWithoutResult(s -> {
                baoCaoDal.updateTrangThai(bc, TrangThai.CHO_XAC_MINH);
                lichSuDal.insert(lichSu(ma, maCG, TrangThai.CHO_XAC_MINH, TrangThai.DA_XAC_MINH,
                        "Xác nhận rầy nâu, mật độ vượt ngưỡng phòng trừ", gui.plusHours(2)));
                thongBaoService.taoThongBao(nguoiGui, LoaiThongBao.KET_QUA_XAC_MINH, ma,
                        "Báo cáo đã được xác minh: " + bc.getTieuDe(), "Chuyên gia xác nhận: Rầy nâu.");
            });
        }

        // 3 báo cáo chờ xác minh, báo cáo thứ 3 gửi 3 ngày trước nên đã quá hạn 48 giờ (BR02)
        int a = taoBaoCaoCho(ndBay, maKV, "Rầy nâu bám dày gốc lúa, có chòm cháy rầy",
                "Thăm đồng sáng nay thấy rầy nâu bám dày gốc lúa, khoảng 3.000 con/m². Giữa ruộng có 2 chòm lúa vàng khô, "
                        + "diện tích ảnh hưởng khoảng 600 m². Lúa đang làm đòng.",
                3, bayGio.minusHours(2), tam, "ray-nau-1.jpg", "ray-nau-2.jpg");
        int b = taoBaoCaoCho(ndMai, maKV, "Lá lúa có vết hình thoi, tâm xám, viền nâu",
                "Lá lúa có nhiều vết hình thoi màu xám, viền nâu, tập trung ở chỗ sạ dày. Tuần này sương nhiều, "
                        + "trời âm u. Đã bón đợt 2 được 5 ngày.",
                2, bayGio.minusHours(5), tam, "dao-on-1.jpg", "dao-on-2.jpg");
        int c = taoBaoCaoCho(ndTu, maKV, "Lúa vàng lá từng chòm, chưa rõ nguyên nhân",
                "Lúa vàng lá từ lá dưới lên, cây thấp hơn xung quanh, rải rác từng chòm khoảng 300 m². "
                        + "Không thấy sâu hay rầy rõ ràng.",
                1, bayGio.minusDays(3), tam, "vang-la-1.jpg", "ruong-1.jpg");
        tx.executeWithoutResult(s -> {
            for (int ma : new int[]{a, b, c}) {
                BaoCaoHienTruong bc = baoCaoDal.getById(ma);
                thongBaoService.taoThongBao(maCG, LoaiThongBao.BAO_CAO_MOI, ma, "Báo cáo mới: " + bc.getTieuDe(),
                        "Có báo cáo mới cần xác minh trong khu vực " + kv.getTenKhuVuc() + ".");
            }
        });

        // 1 báo cáo bị bác bỏ hôm qua, nông dân còn được gửi lại 1 lần (BR04)
        LocalDateTime guiBacBo = bayGio.minusDays(1).minusHours(6);
        int d = taoBaoCaoCho(ndBay, maKV, "Có nhiều bướm nhỏ màu vàng bay trong ruộng",
                "Chiều qua lội ruộng thấy nhiều bướm nhỏ màu vàng nâu bay lên, không biết là bướm gì.",
                1, guiBacBo, tam, "cuon-la-2.jpg");
        BaoCaoHienTruong bd = baoCaoDal.getById(d);
        bd.setTrangThai(TrangThai.BI_BAC_BO);
        bd.setNgayVaoTrangThai(guiBacBo.plusHours(4));
        String lyDo = "Ảnh chỉ thấy con bướm, chưa thấy lá lúa bị hại. Đề nghị chụp cận cảnh lá bị cuốn hoặc có vệt trắng rồi gửi lại.";
        tx.executeWithoutResult(s -> {
            baoCaoDal.updateTrangThai(bd, TrangThai.CHO_XAC_MINH);
            lichSuDal.insert(lichSu(d, maCG, TrangThai.CHO_XAC_MINH, TrangThai.BI_BAC_BO, lyDo, guiBacBo.plusHours(4)));
            thongBaoService.taoThongBao(ndBay, LoaiThongBao.KET_QUA_XAC_MINH, d, "Báo cáo bị bác bỏ: " + bd.getTieuDe(),
                    "Lý do: " + lyDo + " Bạn có thể bổ sung và gửi lại 1 lần trong 7 ngày.");
        });
    }

    /** Huấn luyện và kích hoạt phiên bản mô hình đầu tiên để dự báo nguy cơ chạy được ngay. */
    private void huanLuyenMoHinhDau() {
        try {
            MoHinhAI mh = aiService.huanLuyenLai();
            if (aiService.layDangSuDung() == null) {
                aiService.kichHoat(mh.getMaMoHinh());
            }
        } catch (RuntimeException e) {
            LOG.warn("Chưa huấn luyện được mô hình đầu tiên: {}", e.getMessage());
        }
    }

    // ---------------------------------------------------------------- tiện ích

    private int taoTaiKhoan(String ten, String hoTen, String vaiTro, String dienThoai) {
        NguoiDung co = nguoiDungDal.getByTenDangNhap(ten);
        if (co != null) {
            return co.getMaNguoiDung();
        }
        NguoiDung nd = new NguoiDung();
        nd.setTenDangNhap(ten);
        nd.setHoTen(hoTen);
        nd.setDienThoai(dienThoai);
        nd.setVaiTro(vaiTro);
        nd.setMuoi(MatKhau.taoMuoi());
        nd.setMatKhauHash(MatKhau.bam(MAT_KHAU, nd.getMuoi()));
        return nguoiDungDal.insert(nd);
    }

    /** Báo cáo Chờ xác minh kèm ảnh và dòng lịch sử đầu tiên. */
    private int taoBaoCaoCho(int maND, int maKV, String tieuDe, String moTa, int mucDo, LocalDateTime gui, double[] tam,
                             String... anh) {
        BaoCaoHienTruong bc = baoCaoMoi(maND, maKV, tieuDe, moTa, mucDo, gui, tam);
        Integer ma = tx.execute(s -> {
            int m = baoCaoDal.insert(bc);
            for (String a : anh) {
                themAnh(m, a);
            }
            lichSuDal.insert(lichSu(m, maND, null, TrangThai.CHO_XAC_MINH, "Gửi báo cáo", gui));
            return m;
        });
        return ma == null ? 0 : ma;
    }

    private BaoCaoHienTruong baoCaoMoi(int maND, int maKV, String tieuDe, String moTa, int mucDo, LocalDateTime gui,
                                       double[] tam) {
        BaoCaoHienTruong bc = new BaoCaoHienTruong();
        bc.setTieuDe(tieuDe);
        bc.setTenCayTrong("Lúa");
        bc.setMoTa(moTa);
        bc.setMucDo(mucDo);
        bc.setViDo(BigDecimal.valueOf(tam[0] + (r.nextDouble() - 0.5) * 0.02).setScale(6, RoundingMode.HALF_UP));
        bc.setKinhDo(BigDecimal.valueOf(tam[1] + (r.nextDouble() - 0.5) * 0.02).setScale(6, RoundingMode.HALF_UP));
        bc.setTrangThai(TrangThai.CHO_XAC_MINH);
        bc.setNgayPhatHien(gui.toLocalDate());
        bc.setNgayGui(gui);
        bc.setMaNguoiGui(maND);
        bc.setMaKhuVuc(maKV);
        return bc;
    }

    private String moTa(NoiDung nd) {
        String coSo = nd.moTa().contains("con/khóm") ? String.format(nd.moTa(), 15 + r.nextInt(40))
                : nd.moTa().contains("sâu/m²") ? String.format(nd.moTa(), 20 + r.nextInt(60))
                : String.format(nd.moTa(), 5 + r.nextInt(30));
        return coSo + " Lúa giai đoạn " + chon(GIAI_DOAN) + ".";
    }

    private void themAnh(int maBaoCao, String tenAnh) {
        HinhAnh ha = new HinhAnh();
        ha.setMaBaoCao(maBaoCao);
        ha.setDuongDan(anhDaLuu.computeIfAbsent(tenAnh, this::luuAnhMau));
        hinhAnhDal.insert(ha);
    }

    private String luuAnhMau(String tenAnh) {
        try (InputStream in = new ClassPathResource("anh-mau/" + tenAnh).getInputStream()) {
            byte[] noiDung = in.readAllBytes();
            return anhStorage.luu(new FileTai(tenAnh, noiDung.length, noiDung));
        } catch (IOException e) {
            throw new UncheckedIOException("Không đọc được ảnh mẫu " + tenAnh, e);
        }
    }

    private static NoiDung noiDung(LoaiSauBenh loai) {
        NoiDung nd = NOI_DUNG.get(loai.getTenLoai());
        return nd != null ? nd : new NoiDung(new String[]{loai.getTenLoai() + " trên ruộng lúa"},
                "Phát hiện triệu chứng nghi " + loai.getTenLoai().toLowerCase() + ", khoảng %d%% diện tích.",
                new String[]{"ruong-1.jpg"});
    }

    private static LoaiSauBenh timLoai(List<LoaiSauBenh> ds, String ten) {
        return ds.stream().filter(l -> l.getTenLoai().equals(ten)).findFirst().orElse(ds.get(0));
    }

    private String chon(String[] ds) {
        return ds[r.nextInt(ds.length)];
    }

    private static LichSuXacMinh lichSu(int ma, Integer nguoi, String cu, String moi, String ghiChu, LocalDateTime tg) {
        LichSuXacMinh ls = BaoCaoService.lichSu(ma, nguoi, cu, moi, ghiChu);
        ls.setThoiGian(tg);
        return ls;
    }

    private int poisson(double lambda) {
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

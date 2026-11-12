package vn.agr05.bll;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import vn.agr05.dal.IBaoCaoDal;
import vn.agr05.dal.IHinhAnhDal;
import vn.agr05.dal.ILichSuDal;
import vn.agr05.dal.ILoaiSauBenhDal;
import vn.agr05.dal.INguoiDungDal;
import vn.agr05.entity.BaoCaoHienTruong;
import vn.agr05.entity.DuBaoNguyCo;
import vn.agr05.entity.LoaiThongBao;
import vn.agr05.entity.NghiepVuException;
import vn.agr05.entity.ThamSo;
import vn.agr05.entity.TrangThai;

/** Danh sách chờ, duyệt, từ chối, đánh dấu đã xử lý (UC05–UC10). Gọi AIService sau khi Commit. */
@Service
public class XacMinhService {

    static final String LOI_DA_XU_LY = "Báo cáo đã được xử lý.";

    private final IBaoCaoDal baoCaoDal;
    private final IHinhAnhDal hinhAnhDal;
    private final ILichSuDal lichSuDal;
    private final INguoiDungDal nguoiDungDal;
    private final ILoaiSauBenhDal loaiSauBenhDal;
    private final ThongBaoService thongBaoService;
    private final CanhBaoService canhBaoService;
    private final ThamSoService thamSoService;
    private final AIService aiService;
    private final TransactionTemplate tx;

    public XacMinhService(IBaoCaoDal baoCaoDal, IHinhAnhDal hinhAnhDal, ILichSuDal lichSuDal, INguoiDungDal nguoiDungDal,
                          ILoaiSauBenhDal loaiSauBenhDal, ThongBaoService thongBaoService, CanhBaoService canhBaoService,
                          ThamSoService thamSoService, AIService aiService, TransactionTemplate tx) {
        this.baoCaoDal = baoCaoDal;
        this.hinhAnhDal = hinhAnhDal;
        this.lichSuDal = lichSuDal;
        this.nguoiDungDal = nguoiDungDal;
        this.loaiSauBenhDal = loaiSauBenhDal;
        this.thongBaoService = thongBaoService;
        this.canhBaoService = canhBaoService;
        this.thamSoService = thamSoService;
        this.aiService = aiService;
        this.tx = tx;
    }

    /** Mục 3.3.3: báo cáo Chờ xác minh trong khu vực phụ trách; gắn cờ quá hạn để giao diện tô màu. */
    public List<BaoCaoHienTruong> layDanhSachCho(int maCG) {
        List<Integer> dsKV = nguoiDungDal.getKhuVucPhuTrach(maCG);
        int hanGio = thamSoService.layGiaTri(ThamSo.HAN_XAC_MINH_GIO);
        List<BaoCaoHienTruong> ds = baoCaoDal.getByTrangThai(TrangThai.CHO_XAC_MINH, dsKV);
        for (BaoCaoHienTruong bc : ds) {
            bc.setQuaHan(bc.isQuaHan() || bc.laQuaHan(hanGio));
        }
        return ds;
    }

    /** Báo cáo Đã xác minh trong khu vực phụ trách, để chuyên gia đánh dấu đã xử lý (UC10). */
    public List<BaoCaoHienTruong> layDaXacMinh(int maCG) {
        return baoCaoDal.getByTrangThai(TrangThai.DA_XAC_MINH, nguoiDungDal.getKhuVucPhuTrach(maCG));
    }

    /** Mục 3.3.3: chi tiết, ảnh, lịch sử và dự báo (nếu có). Chỉ xem được báo cáo trong khu vực phụ trách (BR05). */
    public BaoCaoHienTruong layChiTiet(int ma, int maCG) {
        BaoCaoHienTruong bc = layBaoCao(ma);
        if (!nguoiDungDal.getKhuVucPhuTrach(maCG).contains(bc.getMaKhuVuc())) {
            throw NghiepVuException.khongDuQuyen("Bạn không phụ trách khu vực của báo cáo này.");
        }
        bc.setDsHinhAnh(hinhAnhDal.getByBaoCao(ma));
        bc.setDsLichSu(lichSuDal.getByBaoCao(ma));
        bc.setDuBao(aiService.layDuBaoCuaBaoCao(ma));
        int hanGio = thamSoService.layGiaTri(ThamSo.HAN_XAC_MINH_GIO);
        bc.setQuaHan(bc.isQuaHan() || bc.laQuaHan(hanGio));
        return bc;
    }

    /**
     * Mục 3.3.4: kiểm tra loại (BR04) → trạng thái (BR03) → quyền (BR05); một transaction gồm đổi trạng thái,
     * lịch sử, thông báo, BR06. Sau Commit mới gọi AI (BR08).
     */
    public boolean xacMinh(int ma, Integer maLoai, String kn, int maCG) {
        if (maLoai == null) {
            throw NghiepVuException.khongHopLe("Vui lòng chọn loại sâu bệnh trước khi duyệt.", "maLoai");
        }
        if (loaiSauBenhDal.getById(maLoai) == null) {
            throw NghiepVuException.khongHopLe("Loại sâu bệnh không có trong danh mục.", "maLoai");
        }
        BaoCaoHienTruong bc = layBaoCao(ma);
        if (!TrangThai.CHO_XAC_MINH.equals(bc.getTrangThai())) {
            throw NghiepVuException.xungDot(LOI_DA_XU_LY);
        }
        kiemTraQuyen(bc, maCG);

        bc.setMaLoai(maLoai);
        bc.setKhuyenNghi(rongThanhNull(kn));
        bc.chuyenTrangThai(TrangThai.DA_XAC_MINH);
        tx.executeWithoutResult(s -> {
            if (!baoCaoDal.updateTrangThai(bc, TrangThai.CHO_XAC_MINH)) {
                throw NghiepVuException.xungDot(LOI_DA_XU_LY);
            }
            lichSuDal.insert(BaoCaoService.lichSu(ma, maCG, TrangThai.CHO_XAC_MINH, TrangThai.DA_XAC_MINH,
                    rongThanhNull(kn)));
            thongBaoService.taoThongBao(bc.getMaNguoiGui(), LoaiThongBao.KET_QUA_XAC_MINH, ma,
                    "Báo cáo đã được xác minh: " + bc.getTieuDe(),
                    "Chuyên gia đã xác minh báo cáo." + (bc.getKhuyenNghi() == null ? "" : " Khuyến nghị: " + bc.getKhuyenNghi()));
            canhBaoService.kiemTraNguong(bc.getMaKhuVuc(), maLoai);
        });
        // Ngoài transaction: AI lỗi thì trả null, việc duyệt vẫn giữ (ARCH05, luật 11)
        aiService.duBaoNguyCo(bc);
        return true;
    }

    /** Mục 3.3.5. Báo cáo đã gửi lại một lần mà vẫn bị bác bỏ thì đóng hồ sơ trong cùng transaction (BR04). */
    public boolean tuChoi(int ma, String lyDo, int maCG) {
        if (lyDo == null || lyDo.isBlank()) {
            throw NghiepVuException.khongHopLe("Vui lòng nhập lý do từ chối.", "khuyenNghi");
        }
        BaoCaoHienTruong bc = layBaoCao(ma);
        if (!TrangThai.CHO_XAC_MINH.equals(bc.getTrangThai())) {
            throw NghiepVuException.xungDot(LOI_DA_XU_LY);
        }
        kiemTraQuyen(bc, maCG);

        String lyDoGon = lyDo.trim();
        bc.chuyenTrangThai(TrangThai.BI_BAC_BO);
        boolean dongHoSo = bc.getSoLanGuiLai() >= 1;
        tx.executeWithoutResult(s -> {
            if (!baoCaoDal.updateTrangThai(bc, TrangThai.CHO_XAC_MINH)) {
                throw NghiepVuException.xungDot(LOI_DA_XU_LY);
            }
            lichSuDal.insert(BaoCaoService.lichSu(ma, maCG, TrangThai.CHO_XAC_MINH, TrangThai.BI_BAC_BO, lyDoGon));
            thongBaoService.taoThongBao(bc.getMaNguoiGui(), LoaiThongBao.KET_QUA_XAC_MINH, ma,
                    "Báo cáo bị bác bỏ: " + bc.getTieuDe(), "Lý do: " + lyDoGon
                            + (dongHoSo ? "" : " Bạn có thể bổ sung và gửi lại 1 lần trong "
                            + thamSoService.layGiaTri(ThamSo.HAN_GUI_LAI_NGAY) + " ngày."));
            if (dongHoSo) {
                bc.chuyenTrangThai(TrangThai.DA_XU_LY);
                if (!baoCaoDal.updateTrangThai(bc, TrangThai.BI_BAC_BO)) {
                    throw NghiepVuException.xungDot(LOI_DA_XU_LY);
                }
                lichSuDal.insert(BaoCaoService.lichSu(ma, null, TrangThai.BI_BAC_BO, TrangThai.DA_XU_LY,
                        "Đã gửi lại một lần mà vẫn bị bác bỏ, hệ thống đóng hồ sơ (BR04)."));
                thongBaoService.taoThongBao(bc.getMaNguoiGui(), LoaiThongBao.DONG_HO_SO, ma,
                        "Đóng hồ sơ: " + bc.getTieuDe(), "Báo cáo đã gửi lại một lần nhưng vẫn bị bác bỏ nên hồ sơ được đóng.");
            }
        });
        return true;
    }

    /** UC10: báo cáo Đã xác minh → Đã xử lý, phải có khuyến nghị. */
    public boolean danhDauDaXuLy(int ma, String ghiChu, int maCG) {
        BaoCaoHienTruong bc = layBaoCao(ma);
        if (!TrangThai.DA_XAC_MINH.equals(bc.getTrangThai())) {
            throw NghiepVuException.xungDot("Chỉ đánh dấu đã xử lý cho báo cáo Đã xác minh.");
        }
        kiemTraQuyen(bc, maCG);
        String kn = rongThanhNull(ghiChu);
        if (kn == null && bc.getKhuyenNghi() == null) {
            throw NghiepVuException.khongHopLe("Vui lòng nhập khuyến nghị hoặc biện pháp đã xử lý.", "khuyenNghi");
        }
        if (kn != null) {
            bc.setKhuyenNghi(kn);
        }
        bc.chuyenTrangThai(TrangThai.DA_XU_LY);
        tx.executeWithoutResult(s -> {
            if (!baoCaoDal.updateTrangThai(bc, TrangThai.DA_XAC_MINH)) {
                throw NghiepVuException.xungDot(LOI_DA_XU_LY);
            }
            lichSuDal.insert(BaoCaoService.lichSu(ma, maCG, TrangThai.DA_XAC_MINH, TrangThai.DA_XU_LY, bc.getKhuyenNghi()));
            thongBaoService.taoThongBao(bc.getMaNguoiGui(), LoaiThongBao.KET_QUA_XAC_MINH, ma,
                    "Báo cáo đã được xử lý: " + bc.getTieuDe(), "Biện pháp / khuyến nghị: " + bc.getKhuyenNghi());
        });
        return true;
    }

    /** Dự báo đã lưu của báo cáo (để API duyệt trả về cho trang). */
    public DuBaoNguyCo layDuBao(int ma) {
        return aiService.layDuBaoCuaBaoCao(ma);
    }

    private BaoCaoHienTruong layBaoCao(int ma) {
        BaoCaoHienTruong bc = baoCaoDal.getById(ma);
        if (bc == null) {
            throw NghiepVuException.khongTimThay("Không tìm thấy báo cáo.");
        }
        return bc;
    }

    /** BR05: đúng khu vực phụ trách và không phải báo cáo do chính mình gửi. */
    private void kiemTraQuyen(BaoCaoHienTruong bc, int maCG) {
        if (!nguoiDungDal.getKhuVucPhuTrach(maCG).contains(bc.getMaKhuVuc())) {
            throw NghiepVuException.khongDuQuyen("Bạn không phụ trách khu vực của báo cáo này.");
        }
        if (bc.getMaNguoiGui() != null && bc.getMaNguoiGui() == maCG) {
            throw NghiepVuException.khongDuQuyen("Không được xác minh báo cáo của chính mình.");
        }
    }

    private static String rongThanhNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}

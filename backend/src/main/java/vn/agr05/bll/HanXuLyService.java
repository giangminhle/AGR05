package vn.agr05.bll;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import vn.agr05.dal.IBaoCaoDal;
import vn.agr05.dal.ILichSuDal;
import vn.agr05.dal.INguoiDungDal;
import vn.agr05.entity.BaoCaoHienTruong;
import vn.agr05.entity.LoaiThongBao;
import vn.agr05.entity.NghiepVuException;
import vn.agr05.entity.ThamSo;
import vn.agr05.entity.TrangThai;

/** Chức năng nền FR-17 (mục 3.3.7). Mỗi báo cáo xử lý trong một transaction riêng. */
@Service
public class HanXuLyService {

    private static final Logger LOG = LoggerFactory.getLogger(HanXuLyService.class);

    private final IBaoCaoDal baoCaoDal;
    private final ILichSuDal lichSuDal;
    private final INguoiDungDal nguoiDungDal;
    private final ThongBaoService thongBaoService;
    private final ThamSoService thamSoService;
    private final TransactionTemplate tx;

    public HanXuLyService(IBaoCaoDal baoCaoDal, ILichSuDal lichSuDal, INguoiDungDal nguoiDungDal,
                          ThongBaoService thongBaoService, ThamSoService thamSoService, TransactionTemplate tx) {
        this.baoCaoDal = baoCaoDal;
        this.lichSuDal = lichSuDal;
        this.nguoiDungDal = nguoiDungDal;
        this.thongBaoService = thongBaoService;
        this.thamSoService = thamSoService;
        this.tx = tx;
    }

    /** BR02: gắn cờ quá hạn và báo Quản trị viên. Trả về số báo cáo vừa gắn cờ. */
    public int kiemTraQuaHan() {
        int hanGio = thamSoService.layGiaTri(ThamSo.HAN_XAC_MINH_GIO);
        int dem = 0;
        for (BaoCaoHienTruong bc : baoCaoDal.getQuaHan(hanGio)) {
            try {
                Boolean ok = tx.execute(s -> {
                    if (!baoCaoDal.danhDauQuaHan(bc.getMaBaoCao())) {
                        return false;
                    }
                    thongBaoService.taoThongBaoChoNhieu(nguoiDungDal.getQuanTri(), LoaiThongBao.QUA_HAN, bc.getMaBaoCao(),
                            "Báo cáo quá hạn: " + bc.getTieuDe(), "Báo cáo #" + bc.getMaBaoCao() + " tại " + bc.getTenKhuVuc()
                                    + " đã quá " + hanGio + " giờ chưa được xác minh. Cần phân công lại.");
                    return true;
                });
                if (Boolean.TRUE.equals(ok)) {
                    dem++;
                }
            } catch (RuntimeException e) {
                LOG.error("Gắn cờ quá hạn lỗi cho báo cáo {}", bc.getMaBaoCao(), e);
            }
        }
        return dem;
    }

    /** BR04: báo cáo Bị bác bỏ quá HAN_GUI_LAI_NGAY chưa gửi lại thì đóng hồ sơ. Trả về số hồ sơ đã đóng. */
    public int dongHoSoHetHan() {
        int hanNgay = thamSoService.layGiaTri(ThamSo.HAN_GUI_LAI_NGAY);
        List<BaoCaoHienTruong> ds = baoCaoDal.getBiBacBoHetHan(hanNgay);
        int dem = 0;
        for (BaoCaoHienTruong bc : ds) {
            try {
                bc.chuyenTrangThai(TrangThai.DA_XU_LY);
                tx.executeWithoutResult(s -> {
                    if (!baoCaoDal.updateTrangThai(bc, TrangThai.BI_BAC_BO)) {
                        throw NghiepVuException.xungDot("Báo cáo đã thay đổi");
                    }
                    lichSuDal.insert(BaoCaoService.lichSu(bc.getMaBaoCao(), null, TrangThai.BI_BAC_BO, TrangThai.DA_XU_LY,
                            "Quá " + hanNgay + " ngày không gửi lại, hệ thống đóng hồ sơ (BR04)."));
                    thongBaoService.taoThongBao(bc.getMaNguoiGui(), LoaiThongBao.DONG_HO_SO, bc.getMaBaoCao(),
                            "Đóng hồ sơ: " + bc.getTieuDe(), "Báo cáo bị bác bỏ đã quá " + hanNgay
                                    + " ngày mà không gửi lại nên hồ sơ được đóng.");
                });
                dem++;
            } catch (RuntimeException e) {
                LOG.error("Đóng hồ sơ lỗi cho báo cáo {}", bc.getMaBaoCao(), e);
            }
        }
        return dem;
    }
}

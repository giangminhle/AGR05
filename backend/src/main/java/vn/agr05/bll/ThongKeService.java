package vn.agr05.bll;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import vn.agr05.dal.IBaoCaoDal;
import vn.agr05.dal.ICanhBaoDal;
import vn.agr05.dal.IDuBaoDal;
import vn.agr05.dal.INguoiDungDal;
import vn.agr05.entity.NghiepVuException;
import vn.agr05.entity.NguoiDung;
import vn.agr05.entity.VaiTro;

/** Đếm báo cáo, ca dịch, cảnh báo và dự báo theo kỳ (UC11). Chuyên gia chỉ thấy khu vực mình phụ trách. */
@Service
public class ThongKeService {

    private final IBaoCaoDal baoCaoDal;
    private final ICanhBaoDal canhBaoDal;
    private final IDuBaoDal duBaoDal;
    private final INguoiDungDal nguoiDungDal;
    private final AppSession appSession;

    public ThongKeService(IBaoCaoDal baoCaoDal, ICanhBaoDal canhBaoDal, IDuBaoDal duBaoDal, INguoiDungDal nguoiDungDal,
                          AppSession appSession) {
        this.baoCaoDal = baoCaoDal;
        this.canhBaoDal = canhBaoDal;
        this.duBaoDal = duBaoDal;
        this.nguoiDungDal = nguoiDungDal;
        this.appSession = appSession;
    }

    public BangThongKe thongKe(LocalDate tu, LocalDate den, Integer maKV, Integer maLoai) {
        LocalDate denNgay = den == null ? LocalDate.now() : den;
        LocalDate tuNgay = tu == null ? denNgay.minusDays(30) : tu;
        if (tuNgay.isAfter(denNgay)) {
            throw NghiepVuException.khongHopLe("Ngày bắt đầu không được sau ngày kết thúc.", "tuNgay");
        }
        NguoiDung nd = appSession.getNguoiDung();
        List<Integer> dsKV;
        if (VaiTro.CHUYEN_GIA.equals(nd.getVaiTro())) {
            List<Integer> phuTrach = nguoiDungDal.getKhuVucPhuTrach(nd.getMaNguoiDung());
            if (maKV != null && !phuTrach.contains(maKV)) {
                throw NghiepVuException.khongDuQuyen("Bạn chỉ xem được thống kê khu vực mình phụ trách.");
            }
            dsKV = maKV == null ? phuTrach : List.of(maKV);
        } else {
            dsKV = maKV == null ? null : List.of(maKV);
        }
        return new BangThongKe(tuNgay, denNgay,
                baoCaoDal.demTheoTrangThai(tuNgay, denNgay, dsKV, maLoai),
                baoCaoDal.demTheoTuan(tuNgay, denNgay, dsKV, maLoai),
                duBaoDal.demTheoMuc(tuNgay, denNgay, dsKV, maLoai),
                canhBaoDal.getTrongKy(tuNgay, denNgay, dsKV, maLoai));
    }
}

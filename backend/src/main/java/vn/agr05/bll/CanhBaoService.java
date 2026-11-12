package vn.agr05.bll;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import vn.agr05.dal.IBaoCaoDal;
import vn.agr05.dal.ICanhBaoDal;
import vn.agr05.dal.IKhuVucDal;
import vn.agr05.dal.ILoaiSauBenhDal;
import vn.agr05.dal.INguoiDungDal;
import vn.agr05.entity.BaoCaoHienTruong;
import vn.agr05.entity.CanhBao;
import vn.agr05.entity.DuBaoNguyCo;
import vn.agr05.entity.KhuVuc;
import vn.agr05.entity.LoaiSauBenh;
import vn.agr05.entity.LoaiThongBao;
import vn.agr05.entity.NguoiDung;
import vn.agr05.entity.ThamSo;

/**
 * Kiểm tra ngưỡng BR06, tạo cảnh báo từ dự báo AI (BR08).
 * Chặn cảnh báo trùng: cùng khu vực, cùng loại đã có cảnh báo (BR06 hoặc AI) trong SO_NGAY_XET_NGUONG ngày thì không tạo nữa.
 */
@Service
public class CanhBaoService {

    private final ICanhBaoDal canhBaoDal;
    private final IBaoCaoDal baoCaoDal;
    private final INguoiDungDal nguoiDungDal;
    private final IKhuVucDal khuVucDal;
    private final ILoaiSauBenhDal loaiSauBenhDal;
    private final ThongBaoService thongBaoService;
    private final ThamSoService thamSoService;

    public CanhBaoService(ICanhBaoDal canhBaoDal, IBaoCaoDal baoCaoDal, INguoiDungDal nguoiDungDal, IKhuVucDal khuVucDal,
                          ILoaiSauBenhDal loaiSauBenhDal, ThongBaoService thongBaoService, ThamSoService thamSoService) {
        this.canhBaoDal = canhBaoDal;
        this.baoCaoDal = baoCaoDal;
        this.nguoiDungDal = nguoiDungDal;
        this.khuVucDal = khuVucDal;
        this.loaiSauBenhDal = loaiSauBenhDal;
        this.thongBaoService = thongBaoService;
        this.thamSoService = thamSoService;
    }

    /** BR06. Chạy bên trong transaction đang mở của người gọi (XacMinhService.xacMinh). */
    public void kiemTraNguong(int maKV, int maLoai) {
        int soNgay = thamSoService.layGiaTri(ThamSo.SO_NGAY_XET_NGUONG);
        int nguong = thamSoService.layGiaTri(ThamSo.NGUONG_CANH_BAO);
        LocalDateTime bayGio = LocalDateTime.now();
        LocalDateTime tu = bayGio.minusDays(soNgay);
        int soCa = baoCaoDal.demCaDaXacMinh(maKV, maLoai, tu, bayGio.plusMinutes(1));
        if (soCa < nguong || canhBaoDal.coCanhBaoTu(maKV, maLoai, tu)) {
            return;
        }
        String tenLoai = tenLoai(maLoai);
        String tenKV = tenKhuVuc(maKV);
        String noiDung = "Cảnh báo dịch " + tenLoai + " tại " + tenKV + ": " + soCa
                + " ca đã xác minh trong " + soNgay + " ngày (ngưỡng " + nguong + ", BR06).";
        CanhBao cb = new CanhBao();
        cb.setMaKhuVuc(maKV);
        cb.setMaLoai(maLoai);
        cb.setNoiDung(noiDung);
        canhBaoDal.insert(cb);
        thongBaoKhuVuc(maKV, null, "Cảnh báo dịch " + tenLoai + " tại " + tenKV, noiDung);
    }

    /**
     * BR08: tạo cảnh báo từ dự báo mức Cao. Đã có cảnh báo trong kỳ thì chỉ giữ dự báo, trả về null (UC18-A1, TC13).
     * Chạy bên trong transaction đang mở của người gọi (AIService.duBaoNguyCo).
     */
    public Integer taoCanhBaoTuDuBao(DuBaoNguyCo duBao, BaoCaoHienTruong bc) {
        int soNgay = thamSoService.layGiaTri(ThamSo.SO_NGAY_XET_NGUONG);
        LocalDateTime tu = LocalDateTime.now().minusDays(soNgay);
        if (canhBaoDal.coCanhBaoTu(bc.getMaKhuVuc(), bc.getMaLoai(), tu)) {
            return null;
        }
        String tenLoai = tenLoai(bc.getMaLoai());
        String tenKV = tenKhuVuc(bc.getMaKhuVuc());
        String noiDung = "Dự báo nguy cơ CAO: " + tenLoai + " có thể bùng phát tại " + tenKV + " trong "
                + duBao.getSoNgayDuBao() + " ngày tới (xác suất " + Math.round(duBao.getXacSuat() * 100) + "%, BR08).";
        CanhBao cb = new CanhBao();
        cb.setMaKhuVuc(bc.getMaKhuVuc());
        cb.setMaLoai(bc.getMaLoai());
        cb.setMaDuBao(duBao.getMaDuBao());
        cb.setNoiDung(noiDung);
        int ma = canhBaoDal.insert(cb);
        thongBaoKhuVuc(bc.getMaKhuVuc(), bc.getMaBaoCao(), "Cảnh báo nguy cơ dịch " + tenLoai + " tại " + tenKV, noiDung);
        return ma;
    }

    /** BR07: cảnh báo gửi cho nông dân và chuyên gia trong khu vực. */
    private void thongBaoKhuVuc(int maKV, Integer maBaoCao, String tieuDe, String noiDung) {
        List<NguoiDung> nhan = new ArrayList<>(nguoiDungDal.getNongDanTheoKhuVuc(maKV));
        nhan.addAll(nguoiDungDal.getChuyenGiaTheoKhuVuc(maKV));
        thongBaoService.taoThongBaoChoNhieu(nhan, LoaiThongBao.CANH_BAO_DICH, maBaoCao, tieuDe, noiDung);
    }

    private String tenLoai(int maLoai) {
        LoaiSauBenh l = loaiSauBenhDal.getById(maLoai);
        return l == null ? "loại " + maLoai : l.getTenLoai();
    }

    private String tenKhuVuc(int maKV) {
        KhuVuc kv = khuVucDal.getById(maKV);
        return kv == null ? "khu vực " + maKV : kv.getTenKhuVuc();
    }
}

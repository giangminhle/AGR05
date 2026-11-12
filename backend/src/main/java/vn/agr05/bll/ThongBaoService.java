package vn.agr05.bll;

import java.util.List;
import org.springframework.stereotype.Service;
import vn.agr05.dal.IThongBaoDal;
import vn.agr05.entity.NguoiDung;
import vn.agr05.entity.ThongBao;

/** Tạo thông báo nội bộ, đếm chưa đọc, đánh dấu đã đọc (BR07). */
@Service
public class ThongBaoService {

    private final IThongBaoDal thongBaoDal;

    public ThongBaoService(IThongBaoDal thongBaoDal) {
        this.thongBaoDal = thongBaoDal;
    }

    /** Chạy bên trong transaction đang mở của người gọi. */
    public int taoThongBao(int maNhan, String loai, Integer maBaoCao, String tieuDe, String noiDung) {
        ThongBao tb = new ThongBao();
        tb.setMaNguoiNhan(maNhan);
        tb.setLoai(loai);
        tb.setMaBaoCao(maBaoCao);
        tb.setTieuDe(catNgan(tieuDe, 200));
        tb.setNoiDung(catNgan(noiDung, 1000));
        return thongBaoDal.insert(tb);
    }

    /** Gửi cùng một thông báo cho nhiều người (không gửi trùng một người hai lần). */
    public void taoThongBaoChoNhieu(List<NguoiDung> dsNhan, String loai, Integer maBaoCao, String tieuDe, String noiDung) {
        dsNhan.stream().map(NguoiDung::getMaNguoiDung).distinct()
                .forEach(ma -> taoThongBao(ma, loai, maBaoCao, tieuDe, noiDung));
    }

    public int demChuaDoc(int ma) {
        return thongBaoDal.countChuaDoc(ma);
    }

    public List<ThongBao> layThongBao(int ma) {
        return thongBaoDal.getByNguoiNhan(ma);
    }

    public boolean danhDauDaDoc(int maTB, int ma) {
        return thongBaoDal.markRead(maTB, ma);
    }

    private static String catNgan(String s, int toiDa) {
        return s != null && s.length() > toiDa ? s.substring(0, toiDa) : s;
    }
}

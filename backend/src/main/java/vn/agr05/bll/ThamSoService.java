package vn.agr05.bll;

import java.util.List;
import org.springframework.stereotype.Service;
import vn.agr05.dal.IThamSoDal;
import vn.agr05.entity.NghiepVuException;
import vn.agr05.entity.ThamSo;
import vn.agr05.entity.ThamSoHeThong;

/** Đọc và cập nhật năm tham số trong ThamSoHeThong (ARCH03, UC16). */
@Service
public class ThamSoService {

    private final IThamSoDal thamSoDal;

    public ThamSoService(IThamSoDal thamSoDal) {
        this.thamSoDal = thamSoDal;
    }

    public int layGiaTri(String ma) {
        Integer v = thamSoDal.getGiaTri(ma);
        if (v == null) {
            throw new IllegalStateException("Thiếu tham số " + ma + " trong bảng ThamSoHeThong");
        }
        return v;
    }

    public List<ThamSoHeThong> layTatCa() {
        return thamSoDal.getAll();
    }

    /** Giá trị phải dương; NGUONG_NGUY_CO trong 1..100 (TC16). */
    public boolean capNhat(String ma, int giaTri) {
        if (thamSoDal.getGiaTri(ma) == null) {
            throw NghiepVuException.khongTimThay("Không có tham số " + ma + ".");
        }
        if (giaTri <= 0) {
            throw NghiepVuException.khongHopLe("Giá trị không hợp lệ: phải là số nguyên dương.", "giaTri");
        }
        if (ThamSo.NGUONG_NGUY_CO.equals(ma) && giaTri > 100) {
            throw NghiepVuException.khongHopLe("Giá trị không hợp lệ: ngưỡng nguy cơ phải từ 1 đến 100 (%).", "giaTri");
        }
        return thamSoDal.update(ma, giaTri);
    }
}

package vn.agr05.bll;

import java.io.Serializable;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import vn.agr05.entity.NghiepVuException;
import vn.agr05.entity.NguoiDung;
import vn.agr05.entity.VaiTro;

/**
 * Người dùng đang đăng nhập và các khu vực phụ trách, lưu trong phiên (session) của Spring.
 * Controller luôn lấy người dùng ở đây, không nhận mã người dùng từ request (luật 8).
 */
@Component
@SessionScope
public class AppSession implements Serializable {

    private NguoiDung nguoiDung;
    private List<Integer> dsMaKhuVuc = List.of();

    public void datNguoiDung(NguoiDung nd, List<Integer> dsMaKhuVuc) {
        this.nguoiDung = nd;
        this.dsMaKhuVuc = dsMaKhuVuc == null ? List.of() : List.copyOf(dsMaKhuVuc);
    }

    public void xoa() {
        nguoiDung = null;
        dsMaKhuVuc = List.of();
    }

    public boolean daDangNhap() {
        return nguoiDung != null;
    }

    public NguoiDung getNguoiDung() {
        if (nguoiDung == null) {
            throw NghiepVuException.khongDuQuyen("Vui lòng đăng nhập.");
        }
        return nguoiDung;
    }

    public int layMa() {
        return getNguoiDung().getMaNguoiDung();
    }

    public String layVaiTro() {
        return getNguoiDung().getVaiTro();
    }

    public boolean laQuanTri() {
        return nguoiDung != null && VaiTro.QUAN_TRI.equals(nguoiDung.getVaiTro());
    }

    public List<Integer> getDsMaKhuVuc() {
        return dsMaKhuVuc;
    }
}

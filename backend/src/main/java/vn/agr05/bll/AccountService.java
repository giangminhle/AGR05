package vn.agr05.bll;

import java.util.List;
import org.springframework.stereotype.Service;
import vn.agr05.dal.INguoiDungDal;
import vn.agr05.entity.MatKhau;
import vn.agr05.entity.NghiepVuException;
import vn.agr05.entity.NguoiDung;
import vn.agr05.entity.VaiTro;

/** Đăng nhập, tạo, sửa và khóa tài khoản (UC01, UC15). */
@Service
public class AccountService {

    static final String LOI_DANG_NHAP = "Tên đăng nhập hoặc mật khẩu không đúng.";

    private final INguoiDungDal nguoiDungDal;
    private final AppSession appSession;

    public AccountService(INguoiDungDal nguoiDungDal, AppSession appSession) {
        this.nguoiDungDal = nguoiDungDal;
        this.appSession = appSession;
    }

    /**
     * Mục 3.3.1: sai tên hoặc sai mật khẩu báo chung một câu; tài khoản khóa báo riêng (UC01-A2).
     * Đúng thì nạp khu vực phụ trách và lưu vào AppSession.
     */
    public NguoiDung dangNhap(String ten, String matKhau) {
        if (ten == null || ten.isBlank() || matKhau == null || matKhau.isEmpty()) {
            throw NghiepVuException.khongHopLe("Vui lòng nhập tên đăng nhập và mật khẩu.", "tenDangNhap");
        }
        NguoiDung nd = nguoiDungDal.getByTenDangNhap(ten.trim());
        if (nd == null || !nd.xacThucMatKhau(matKhau)) {
            throw NghiepVuException.khongHopLe(LOI_DANG_NHAP, null);
        }
        if (nd.isBiKhoa()) {
            throw NghiepVuException.khongDuQuyen("Tài khoản đã bị khóa, liên hệ Quản trị viên.");
        }
        List<Integer> dsKhuVuc = VaiTro.CHUYEN_GIA.equals(nd.getVaiTro())
                ? nguoiDungDal.getKhuVucPhuTrach(nd.getMaNguoiDung()) : List.of();
        appSession.datNguoiDung(nd, dsKhuVuc);
        return nd;
    }

    /** Tạo tài khoản mới, băm mật khẩu PBKDF2 với muối riêng (SEC01). */
    public int taoTaiKhoan(NguoiDung nd, String matKhau) {
        kiemTraThongTin(nd);
        String ten = nd.getTenDangNhap() == null ? "" : nd.getTenDangNhap().trim();
        if (ten.isEmpty() || ten.length() > 50 || !ten.matches("[A-Za-z0-9_.]+")) {
            throw NghiepVuException.khongHopLe(
                    "Tên đăng nhập bắt buộc, tối đa 50 ký tự, chỉ gồm chữ không dấu, số, dấu chấm, gạch dưới.", "tenDangNhap");
        }
        if (nguoiDungDal.getByTenDangNhap(ten) != null) {
            throw NghiepVuException.khongHopLe("Tên đăng nhập đã tồn tại.", "tenDangNhap");
        }
        if (matKhau == null || matKhau.length() < 6) {
            throw NghiepVuException.khongHopLe("Mật khẩu phải có ít nhất 6 ký tự.", "matKhau");
        }
        nd.setTenDangNhap(ten);
        nd.setMuoi(MatKhau.taoMuoi());
        nd.setMatKhauHash(MatKhau.bam(matKhau, nd.getMuoi()));
        nd.setBiKhoa(false);
        return nguoiDungDal.insert(nd);
    }

    /** Sửa họ tên, điện thoại, vai trò (phân quyền, FR-14). */
    public boolean capNhat(NguoiDung nd) {
        NguoiDung cu = nguoiDungDal.getById(nd.getMaNguoiDung());
        if (cu == null) {
            throw NghiepVuException.khongTimThay("Không tìm thấy người dùng.");
        }
        kiemTraThongTin(nd);
        if (appSession.daDangNhap() && appSession.layMa() == nd.getMaNguoiDung()
                && !nd.getVaiTro().equals(cu.getVaiTro())) {
            throw NghiepVuException.khongHopLe("Không thể đổi vai trò của chính tài khoản mình.", "vaiTro");
        }
        return nguoiDungDal.update(nd);
    }

    /** Khóa hoặc mở khóa. Không cho khóa chính tài khoản mình (UC15-A2). */
    public boolean khoaTaiKhoan(int ma, boolean khoa) {
        if (nguoiDungDal.getById(ma) == null) {
            throw NghiepVuException.khongTimThay("Không tìm thấy người dùng.");
        }
        if (khoa && appSession.daDangNhap() && appSession.layMa() == ma) {
            throw NghiepVuException.khongHopLe("Không thể khóa chính tài khoản của mình.", null);
        }
        return nguoiDungDal.updateBiKhoa(ma, khoa);
    }

    /** UC15-A3: chuyên gia còn phụ trách khu vực thì giao diện cảnh báo cần phân công người khác. */
    public boolean dangPhuTrachKhuVuc(int ma) {
        return !nguoiDungDal.getKhuVucPhuTrach(ma).isEmpty();
    }

    public List<NguoiDung> layTatCa() {
        return nguoiDungDal.getAll();
    }

    private static void kiemTraThongTin(NguoiDung nd) {
        if (nd.getHoTen() == null || nd.getHoTen().isBlank()) {
            throw NghiepVuException.khongHopLe("Vui lòng nhập họ tên.", "hoTen");
        }
        if (nd.getDienThoai() != null && !nd.getDienThoai().isBlank() && !nd.getDienThoai().matches("[0-9]{1,15}")) {
            throw NghiepVuException.khongHopLe("Số điện thoại chỉ gồm chữ số, tối đa 15 số.", "dienThoai");
        }
        if (nd.getDienThoai() != null && nd.getDienThoai().isBlank()) {
            nd.setDienThoai(null);
        }
        if (!VaiTro.hopLe(nd.getVaiTro())) {
            throw NghiepVuException.khongHopLe("Vai trò không hợp lệ.", "vaiTro");
        }
    }
}

package vn.agr05.bll;

import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import vn.agr05.dal.INguoiDungDal;
import vn.agr05.entity.MatKhau;
import vn.agr05.entity.NguoiDung;
import vn.agr05.entity.VaiTro;

/** Lần đầu chạy: chưa có tài khoản QUAN_TRI thì tạo tài khoản admin, mật khẩu ngẫu nhiên in ra log. */
@Component
@Order(1)
public class DbSeeder implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DbSeeder.class);
    private static final String KY_TU = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    private final INguoiDungDal nguoiDungDal;

    public DbSeeder(INguoiDungDal nguoiDungDal) {
        this.nguoiDungDal = nguoiDungDal;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            boolean coQuanTri = nguoiDungDal.getAll().stream().anyMatch(n -> VaiTro.QUAN_TRI.equals(n.getVaiTro()));
            if (coQuanTri) {
                return;
            }
            String matKhau = taoMatKhau();
            NguoiDung admin = new NguoiDung();
            admin.setTenDangNhap("admin");
            admin.setHoTen("Quản trị viên");
            admin.setVaiTro(VaiTro.QUAN_TRI);
            admin.setMuoi(MatKhau.taoMuoi());
            admin.setMatKhauHash(MatKhau.bam(matKhau, admin.getMuoi()));
            nguoiDungDal.insert(admin);
            LOG.warn("=== Đã tạo tài khoản quản trị đầu tiên: admin / {} (hãy ghi lại, mật khẩu chỉ in một lần) ===", matKhau);
        } catch (RuntimeException e) {
            LOG.error("DbSeeder không chạy được (kiểm tra CSDL đã tạo bằng db/AGR05.sql chưa): {}", e.getMessage());
        }
    }

    private static String taoMatKhau() {
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(KY_TU.charAt(r.nextInt(KY_TU.length())));
        }
        return sb.toString();
    }
}

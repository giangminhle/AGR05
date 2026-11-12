package vn.agr05.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.agr05.bll.AccountService;
import vn.agr05.entity.NguoiDung;

/** UC15: quản lý người dùng. */
@RestController
@RequestMapping("/api/nguoi-dung")
public class NguoiDungController {

    private final AccountService accountService;

    public NguoiDungController(AccountService accountService) {
        this.accountService = accountService;
    }

    public record YeuCauTaoTaiKhoan(String tenDangNhap, String matKhau, String hoTen, String dienThoai, String vaiTro) {
    }

    public record YeuCauSua(String hoTen, String dienThoai, String vaiTro) {
    }

    public record YeuCauKhoa(boolean khoa) {
    }

    @GetMapping
    public List<NguoiDungDto> layTatCa() {
        return accountService.layTatCa().stream().map(n -> NguoiDungDto.tu(n, null)).toList();
    }

    @PostMapping
    public Map<String, Integer> taoTaiKhoan(@RequestBody YeuCauTaoTaiKhoan yc) {
        NguoiDung nd = new NguoiDung();
        nd.setTenDangNhap(yc.tenDangNhap());
        nd.setHoTen(yc.hoTen());
        nd.setDienThoai(yc.dienThoai());
        nd.setVaiTro(yc.vaiTro());
        return Map.of("maNguoiDung", accountService.taoTaiKhoan(nd, yc.matKhau()));
    }

    @PutMapping("/{ma}")
    public Map<String, Boolean> sua(@PathVariable int ma, @RequestBody YeuCauSua yc) {
        NguoiDung nd = new NguoiDung();
        nd.setMaNguoiDung(ma);
        nd.setHoTen(yc.hoTen());
        nd.setDienThoai(yc.dienThoai());
        nd.setVaiTro(yc.vaiTro());
        return Map.of("thanhCong", accountService.capNhat(nd));
    }

    @PostMapping("/{ma}/khoa")
    public Map<String, Object> khoa(@PathVariable int ma, @RequestBody YeuCauKhoa yc) {
        Map<String, Object> kq = new HashMap<>();
        kq.put("thanhCong", accountService.khoaTaiKhoan(ma, yc.khoa()));
        kq.put("canPhanCongLai", yc.khoa() && accountService.dangPhuTrachKhuVuc(ma));
        return kq;
    }
}

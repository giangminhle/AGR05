package vn.agr05.api;

import java.util.List;
import vn.agr05.entity.NguoiDung;

/** Thông tin người dùng trả về giao diện (không có MatKhauHash, Muoi). */
public record NguoiDungDto(Integer maNguoiDung, String tenDangNhap, String hoTen, String dienThoai, String vaiTro,
                           boolean biKhoa, List<Integer> dsMaKhuVuc) {

    public static NguoiDungDto tu(NguoiDung nd, List<Integer> dsMaKhuVuc) {
        return new NguoiDungDto(nd.getMaNguoiDung(), nd.getTenDangNhap(), nd.getHoTen(), nd.getDienThoai(),
                nd.getVaiTro(), nd.isBiKhoa(), dsMaKhuVuc);
    }
}

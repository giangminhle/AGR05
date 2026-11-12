package vn.agr05.api.dto;

import vn.agr05.entity.NguoiDung;

/** Người dùng rút gọn (bảng chuyên gia của khu vực). */
public record NguoiDungRutGon(Integer maNguoiDung, String tenDangNhap, String hoTen, String vaiTro, boolean biKhoa) {

    public static NguoiDungRutGon tu(NguoiDung nd) {
        return new NguoiDungRutGon(nd.getMaNguoiDung(), nd.getTenDangNhap(), nd.getHoTen(), nd.getVaiTro(), nd.isBiKhoa());
    }
}

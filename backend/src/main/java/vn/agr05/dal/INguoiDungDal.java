package vn.agr05.dal;

import java.util.List;
import vn.agr05.entity.NguoiDung;

public interface INguoiDungDal {

    NguoiDung getByTenDangNhap(String tenDangNhap);

    NguoiDung getById(int maNguoiDung);

    List<NguoiDung> getAll();

    int insert(NguoiDung nd);

    boolean update(NguoiDung nd);

    boolean updateBiKhoa(int maNguoiDung, boolean khoa);

    /** Mã các khu vực chuyên gia phụ trách (PhanCongKhuVuc). */
    List<Integer> getKhuVucPhuTrach(int maNguoiDung);

    /** Chuyên gia chưa bị khóa đang phụ trách khu vực. */
    List<NguoiDung> getChuyenGiaTheoKhuVuc(int maKhuVuc);

    /** Nông dân chưa bị khóa đã từng gửi báo cáo ở khu vực (người nhận cảnh báo dịch). */
    List<NguoiDung> getNongDanTheoKhuVuc(int maKhuVuc);

    /** Quản trị viên chưa bị khóa. */
    List<NguoiDung> getQuanTri();
}

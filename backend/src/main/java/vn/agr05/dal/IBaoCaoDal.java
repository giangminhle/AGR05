package vn.agr05.dal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import vn.agr05.entity.BaoCaoHienTruong;

/** Truy cập bảng BaoCaoHienTruong. Không có hàm xóa báo cáo (RB06, luật 12). */
public interface IBaoCaoDal {

    int insert(BaoCaoHienTruong bc);

    BaoCaoHienTruong getById(int maBaoCao);

    List<BaoCaoHienTruong> getByNguoiGui(int maNguoiGui);

    /** Báo cáo theo trạng thái trong các khu vực (null = mọi khu vực). */
    List<BaoCaoHienTruong> getByTrangThai(String trangThai, List<Integer> dsMaKhuVuc);

    /** Báo cáo Chờ xác minh đã quá hanGio giờ mà chưa gắn cờ quá hạn (BR02). */
    List<BaoCaoHienTruong> getQuaHan(int hanGio);

    boolean danhDauQuaHan(int maBaoCao);

    /** Báo cáo Bị bác bỏ, chưa gửi lại lần nào, đã quá hanNgay ngày (BR04). */
    List<BaoCaoHienTruong> getBiBacBoHetHan(int hanNgay);

    /** Đổi trạng thái (kèm loại, khuyến nghị) với điều kiện WHERE MaTrangThai = trangThaiCu (luật 4). */
    boolean updateTrangThai(BaoCaoHienTruong bc, String trangThaiCu);

    /** Sửa nội dung khi sửa, gửi lại, với điều kiện WHERE MaTrangThai = trangThaiCu (luật 4). */
    boolean updateNoiDung(BaoCaoHienTruong bc, String trangThaiCu);

    /** Số ca đã xác minh cùng khu vực, cùng loại có ngày xác minh trong [tu, den). */
    int demCaDaXacMinh(int maKhuVuc, int maLoai, LocalDateTime tu, LocalDateTime den);

    /** Mức độ trung bình của các ca đã xác minh trong [tu, den); không có ca thì 0. */
    double mucDoTrungBinh(int maKhuVuc, int maLoai, LocalDateTime tu, LocalDateTime den);

    /** Toàn bộ báo cáo Đã xác minh, Đã xử lý có loại và có ngày xác minh, xếp theo ngày xác minh (BR08). */
    List<BaoCaoHienTruong> getDaXacMinh();

    /** Thống kê: số báo cáo theo trạng thái, gửi trong [tu, den]. */
    LinkedHashMap<String, Integer> demTheoTrangThai(LocalDate tu, LocalDate den, List<Integer> dsMaKhuVuc, Integer maLoai);

    /** Thống kê: số ca đã xác minh theo tuần (khóa = ngày đầu tuần yyyy-MM-dd). */
    LinkedHashMap<String, Integer> demTheoTuan(LocalDate tu, LocalDate den, List<Integer> dsMaKhuVuc, Integer maLoai);
}

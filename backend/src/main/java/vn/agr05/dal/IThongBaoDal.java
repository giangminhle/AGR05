package vn.agr05.dal;

import java.util.List;
import vn.agr05.entity.ThongBao;

public interface IThongBaoDal {

    int insert(ThongBao tb);

    int countChuaDoc(int maNguoiNhan);

    List<ThongBao> getByNguoiNhan(int maNguoiNhan);

    /** Đánh dấu đã đọc, chỉ khi thông báo thuộc maNguoiNhan (BR07). */
    boolean markRead(int maThongBao, int maNguoiNhan);
}

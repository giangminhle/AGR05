package vn.agr05.dal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import vn.agr05.entity.CanhBao;

public interface ICanhBaoDal {

    int insert(CanhBao cb);

    /** Đã có cảnh báo cùng khu vực, cùng loại tạo từ thời điểm tu trở đi chưa (chặn cảnh báo trùng). */
    boolean coCanhBaoTu(int maKhuVuc, int maLoai, LocalDateTime tu);

    List<CanhBao> getTrongKy(LocalDate tu, LocalDate den, List<Integer> dsMaKhuVuc, Integer maLoai);
}

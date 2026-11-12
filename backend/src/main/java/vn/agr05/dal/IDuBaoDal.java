package vn.agr05.dal;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import vn.agr05.entity.DuBaoNguyCo;

public interface IDuBaoDal {

    int insert(DuBaoNguyCo db);

    DuBaoNguyCo getByBaoCao(int maBaoCao);

    /** Thống kê: số dự báo theo mức trong kỳ (lọc theo khu vực, loại của báo cáo kích hoạt). */
    LinkedHashMap<String, Integer> demTheoMuc(LocalDate tu, LocalDate den, List<Integer> dsMaKhuVuc, Integer maLoai);
}

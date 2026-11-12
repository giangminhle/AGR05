package vn.agr05.bll;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import vn.agr05.entity.CanhBao;

/** Kết quả thống kê một kỳ (UC11). */
public record BangThongKe(LocalDate tuNgay, LocalDate denNgay, Map<String, Integer> theoTrangThai,
                          Map<String, Integer> theoTuan, Map<String, Integer> duBaoTheoMuc, List<CanhBao> dsCanhBao) {
}

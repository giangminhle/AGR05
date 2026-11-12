package vn.agr05.entity;

import java.util.Map;
import java.util.Set;

/** Mã trạng thái báo cáo (bảng TrangThaiBaoCao) và bảng chuyển trạng thái (BR03). */
public final class TrangThai {

    public static final String CHO_XAC_MINH = "CHO_XAC_MINH";
    public static final String DA_XAC_MINH = "DA_XAC_MINH";
    public static final String BI_BAC_BO = "BI_BAC_BO";
    public static final String DA_XU_LY = "DA_XU_LY";

    /** Trạng thái được phép chuyển tới từ mỗi trạng thái (mục 3.2.1 docs/domain-model.md). */
    private static final Map<String, Set<String>> CHUYEN = Map.of(
            CHO_XAC_MINH, Set.of(DA_XAC_MINH, BI_BAC_BO),
            BI_BAC_BO, Set.of(CHO_XAC_MINH, DA_XU_LY),
            DA_XAC_MINH, Set.of(DA_XU_LY),
            DA_XU_LY, Set.of());

    private TrangThai() {
    }

    /** tu = null nghĩa là Khởi tạo, chỉ được sang Chờ xác minh. */
    public static boolean choPhepChuyen(String tu, String den) {
        if (tu == null) {
            return CHO_XAC_MINH.equals(den);
        }
        return CHUYEN.getOrDefault(tu, Set.of()).contains(den);
    }

    public static String ten(String ma) {
        if (ma == null) {
            return "Khởi tạo";
        }
        return switch (ma) {
            case CHO_XAC_MINH -> "Chờ xác minh";
            case DA_XAC_MINH -> "Đã xác minh";
            case BI_BAC_BO -> "Bị bác bỏ";
            case DA_XU_LY -> "Đã xử lý";
            default -> ma;
        };
    }
}

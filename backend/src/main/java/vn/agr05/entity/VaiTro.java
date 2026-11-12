package vn.agr05.entity;

/** Mã vai trò (bảng VaiTro). */
public final class VaiTro {

    public static final String NONG_DAN = "NONG_DAN";
    public static final String CHUYEN_GIA = "CHUYEN_GIA";
    public static final String QUAN_TRI = "QUAN_TRI";

    private VaiTro() {
    }

    public static boolean hopLe(String ma) {
        return NONG_DAN.equals(ma) || CHUYEN_GIA.equals(ma) || QUAN_TRI.equals(ma);
    }
}

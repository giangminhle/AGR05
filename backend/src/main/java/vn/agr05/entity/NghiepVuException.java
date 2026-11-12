package vn.agr05.entity;

/**
 * Lỗi nghiệp vụ. Tầng api đổi thành HTTP 400/403/404/409 kèm JSON { "loi": ..., "truong": ... } (luật 15).
 */
public class NghiepVuException extends RuntimeException {

    public enum Loai {
        KHONG_HOP_LE(400), KHONG_DU_QUYEN(403), KHONG_TIM_THAY(404), XUNG_DOT(409);

        private final int maHttp;

        Loai(int maHttp) {
            this.maHttp = maHttp;
        }

        public int getMaHttp() {
            return maHttp;
        }
    }

    private final Loai loai;
    private final String truong;

    public NghiepVuException(Loai loai, String loi, String truong) {
        super(loi);
        this.loai = loai;
        this.truong = truong;
    }

    public static NghiepVuException khongHopLe(String loi, String truong) {
        return new NghiepVuException(Loai.KHONG_HOP_LE, loi, truong);
    }

    public static NghiepVuException khongDuQuyen(String loi) {
        return new NghiepVuException(Loai.KHONG_DU_QUYEN, loi, null);
    }

    public static NghiepVuException khongTimThay(String loi) {
        return new NghiepVuException(Loai.KHONG_TIM_THAY, loi, null);
    }

    public static NghiepVuException xungDot(String loi) {
        return new NghiepVuException(Loai.XUNG_DOT, loi, null);
    }

    public Loai getLoai() {
        return loai;
    }

    public String getTruong() {
        return truong;
    }
}

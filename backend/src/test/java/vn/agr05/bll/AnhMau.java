package vn.agr05.bll;

/** File ảnh mẫu cho test. */
final class AnhMau {

    private AnhMau() {
    }

    static FileTai jpg(String ten, long kichThuoc) {
        byte[] b = new byte[64];
        b[0] = (byte) 0xFF;
        b[1] = (byte) 0xD8;
        b[2] = (byte) 0xFF;
        return new FileTai(ten, kichThuoc, b);
    }

    static FileTai jpg(String ten) {
        return jpg(ten, 2L * 1024 * 1024);
    }
}

package vn.agr05.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Đọc mục agr05 trong application.yml (luật 13). */
@ConfigurationProperties(prefix = "agr05")
public class CauHinhUngDung {

    /** Thư mục lưu file ảnh hiện trường (ARCH02). */
    private String thuMucAnh = "du-lieu/anh";

    /** Thư mục lưu file mô hình AI. */
    private String thuMucMoHinh = "du-lieu/mo-hinh";

    public String getThuMucAnh() {
        return thuMucAnh;
    }

    public void setThuMucAnh(String thuMucAnh) {
        this.thuMucAnh = thuMucAnh;
    }

    public String getThuMucMoHinh() {
        return thuMucMoHinh;
    }

    public void setThuMucMoHinh(String thuMucMoHinh) {
        this.thuMucMoHinh = thuMucMoHinh;
    }
}

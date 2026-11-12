package vn.agr05.bll;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.agr05.config.CauHinhUngDung;

/** Kiểm tra định dạng, dung lượng ảnh; lưu và xóa file ảnh (ARCH02, luật 10). CSDL chỉ lưu tên file. */
@Component
public class AnhStorage {

    public static final long TOI_DA_BYTE = 5L * 1024 * 1024;

    private static final Logger LOG = LoggerFactory.getLogger(AnhStorage.class);
    private static final byte[] CHU_KY_JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] CHU_KY_PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    private final Path thuMuc;

    @Autowired
    public AnhStorage(CauHinhUngDung cauHinh) {
        this(Paths.get(cauHinh.getThuMucAnh()));
    }

    public AnhStorage(Path thuMuc) {
        this.thuMuc = thuMuc.toAbsolutePath().normalize();
    }

    /** true nếu file là JPG/PNG thật (đúng phần mở rộng và chữ ký) và không quá 5 MB. */
    public boolean kiemTraFile(FileTai file) {
        return lyDoLoi(file) == null;
    }

    /** Câu báo lỗi cho file không hợp lệ, null nếu hợp lệ. */
    public String lyDoLoi(FileTai file) {
        String ten = file.tenFile() == null ? "" : file.tenFile();
        String duoi = duoiFile(ten);
        if (!duoi.equals(".jpg") && !duoi.equals(".jpeg") && !duoi.equals(".png")) {
            return "Ảnh " + ten + " không đúng định dạng, chỉ nhận JPG hoặc PNG.";
        }
        if (file.kichThuoc() <= 0 || file.noiDung() == null || file.noiDung().length == 0) {
            return "Ảnh " + ten + " rỗng.";
        }
        if (file.kichThuoc() > TOI_DA_BYTE) {
            return "Ảnh " + ten + " vượt quá 5 MB.";
        }
        boolean dungChuKy = duoi.equals(".png") ? batDauBang(file.noiDung(), CHU_KY_PNG)
                : batDauBang(file.noiDung(), CHU_KY_JPG);
        if (!dungChuKy) {
            return "Ảnh " + ten + " không phải file JPG/PNG hợp lệ.";
        }
        return null;
    }

    /** Lưu file với tên ngẫu nhiên, trả về đường dẫn tương đối để ghi vào HinhAnh.DuongDan. */
    public String luu(FileTai file) {
        try {
            Files.createDirectories(thuMuc);
            String ten = UUID.randomUUID() + duoiFile(file.tenFile());
            Files.write(thuMuc.resolve(ten), file.noiDung());
            return ten;
        } catch (IOException e) {
            throw new UncheckedIOException("Không lưu được file ảnh", e);
        }
    }

    public void xoa(String duongDan) {
        try {
            Files.deleteIfExists(viTri(duongDan));
        } catch (IOException | RuntimeException e) {
            LOG.warn("Không xóa được file ảnh {}: {}", duongDan, e.getMessage());
        }
    }

    public AnhTai doc(String duongDan) {
        try {
            String kieu = duoiFile(duongDan).equals(".png") ? "image/png" : "image/jpeg";
            return new AnhTai(Files.readAllBytes(viTri(duongDan)), kieu);
        } catch (IOException e) {
            throw new UncheckedIOException("Không đọc được file ảnh", e);
        }
    }

    /** Chặn đường dẫn kiểu ../ đi ra ngoài thư mục ảnh. */
    private Path viTri(String duongDan) {
        Path p = thuMuc.resolve(duongDan).normalize();
        if (!p.startsWith(thuMuc)) {
            throw new IllegalArgumentException("Đường dẫn ảnh không hợp lệ");
        }
        return p;
    }

    private static String duoiFile(String ten) {
        int i = ten == null ? -1 : ten.lastIndexOf('.');
        return i < 0 ? "" : ten.substring(i).toLowerCase(Locale.ROOT);
    }

    private static boolean batDauBang(byte[] du, byte[] chuKy) {
        if (du.length < chuKy.length) {
            return false;
        }
        for (int i = 0; i < chuKy.length; i++) {
            if (du[i] != chuKy[i]) {
                return false;
            }
        }
        return true;
    }
}

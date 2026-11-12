package vn.agr05.entity;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Băm mật khẩu PBKDF2WithHmacSHA256, 100 000 vòng, khóa 256 bit, muối 16 byte (SEC01, luật 9). */
public final class MatKhau {

    private static final int SO_VONG = 100_000;
    private static final int DO_DAI_KHOA = 256;
    private static final int DO_DAI_MUOI = 16;
    private static final SecureRandom NGAU_NHIEN = new SecureRandom();

    private MatKhau() {
    }

    /** Tạo muối ngẫu nhiên mới, dạng Base64. */
    public static String taoMuoi() {
        byte[] muoi = new byte[DO_DAI_MUOI];
        NGAU_NHIEN.nextBytes(muoi);
        return Base64.getEncoder().encodeToString(muoi);
    }

    /** Băm mật khẩu với muối (Base64), trả về Base64. */
    public static String bam(String matKhau, String muoiBase64) {
        try {
            PBEKeySpec spec = new PBEKeySpec(matKhau.toCharArray(), Base64.getDecoder().decode(muoiBase64),
                    SO_VONG, DO_DAI_KHOA);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            spec.clearPassword();
            return Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Không băm được mật khẩu", e);
        }
    }

    /** So sánh thời gian hằng bằng MessageDigest.isEqual. */
    public static boolean khop(String matKhau, String muoiBase64, String hashDaLuu) {
        byte[] a = bam(matKhau, muoiBase64).getBytes(StandardCharsets.US_ASCII);
        byte[] b = hashDaLuu.getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(a, b);
    }
}

package vn.agr05.bll;

/** File người dùng tải lên (tách khỏi kiểu MultipartFile của tầng web để test được BLL). */
public record FileTai(String tenFile, long kichThuoc, byte[] noiDung) {
}

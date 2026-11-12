package vn.agr05.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Báo cáo hiện trường của nông dân (bảng BaoCaoHienTruong). */
public class BaoCaoHienTruong {

    private Integer maBaoCao;
    private String tieuDe;
    private String tenCayTrong;
    private String moTa;
    private Integer mucDo;
    private BigDecimal viDo;
    private BigDecimal kinhDo;
    private String trangThai;
    private String khuyenNghi;
    private LocalDate ngayPhatHien;
    private LocalDateTime ngayGui;
    private LocalDateTime ngayVaoTrangThai;
    private int soLanGuiLai;
    private boolean quaHan;
    private Integer maNguoiGui;
    private Integer maKhuVuc;
    private Integer maLoai;
    /** Tên khu vực (lấy kèm khi đọc, không phải cột). */
    private String tenKhuVuc;
    /** Tên loại sâu bệnh (lấy kèm khi đọc). */
    private String tenLoai;
    /** Họ tên người gửi (lấy kèm khi đọc). */
    private String tenNguoiGui;
    /** Thời điểm chuyển sang Đã xác minh, lấy từ LichSuXacMinh (dùng cho AI). */
    private LocalDateTime ngayXacMinh;
    /** Ảnh của báo cáo (hợp thành 1 - 1..5). */
    private List<HinhAnh> dsHinhAnh = new ArrayList<>();
    /** Lịch sử xử lý (hợp thành 1 - 0..*). */
    private List<LichSuXacMinh> dsLichSu = new ArrayList<>();
    /** Nông dân còn được sửa, gửi lại không (Service tính bằng choPhepSua, chỉ để giao diện bật nút). */
    private boolean duocSua;
    /** Dự báo nguy cơ sau khi duyệt (nếu có). */
    private DuBaoNguyCo duBao;

    public Integer getMaBaoCao() {
        return maBaoCao;
    }

    public void setMaBaoCao(Integer maBaoCao) {
        this.maBaoCao = maBaoCao;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getTenCayTrong() {
        return tenCayTrong;
    }

    public void setTenCayTrong(String tenCayTrong) {
        this.tenCayTrong = tenCayTrong;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Integer getMucDo() {
        return mucDo;
    }

    public void setMucDo(Integer mucDo) {
        this.mucDo = mucDo;
    }

    public BigDecimal getViDo() {
        return viDo;
    }

    public void setViDo(BigDecimal viDo) {
        this.viDo = viDo;
    }

    public BigDecimal getKinhDo() {
        return kinhDo;
    }

    public void setKinhDo(BigDecimal kinhDo) {
        this.kinhDo = kinhDo;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getKhuyenNghi() {
        return khuyenNghi;
    }

    public void setKhuyenNghi(String khuyenNghi) {
        this.khuyenNghi = khuyenNghi;
    }

    public LocalDate getNgayPhatHien() {
        return ngayPhatHien;
    }

    public void setNgayPhatHien(LocalDate ngayPhatHien) {
        this.ngayPhatHien = ngayPhatHien;
    }

    public LocalDateTime getNgayGui() {
        return ngayGui;
    }

    public void setNgayGui(LocalDateTime ngayGui) {
        this.ngayGui = ngayGui;
    }

    public LocalDateTime getNgayVaoTrangThai() {
        return ngayVaoTrangThai;
    }

    public void setNgayVaoTrangThai(LocalDateTime ngayVaoTrangThai) {
        this.ngayVaoTrangThai = ngayVaoTrangThai;
    }

    public int getSoLanGuiLai() {
        return soLanGuiLai;
    }

    public void setSoLanGuiLai(int soLanGuiLai) {
        this.soLanGuiLai = soLanGuiLai;
    }

    public boolean isQuaHan() {
        return quaHan;
    }

    public void setQuaHan(boolean quaHan) {
        this.quaHan = quaHan;
    }

    public Integer getMaNguoiGui() {
        return maNguoiGui;
    }

    public void setMaNguoiGui(Integer maNguoiGui) {
        this.maNguoiGui = maNguoiGui;
    }

    public Integer getMaKhuVuc() {
        return maKhuVuc;
    }

    public void setMaKhuVuc(Integer maKhuVuc) {
        this.maKhuVuc = maKhuVuc;
    }

    public Integer getMaLoai() {
        return maLoai;
    }

    public void setMaLoai(Integer maLoai) {
        this.maLoai = maLoai;
    }

    public String getTenKhuVuc() {
        return tenKhuVuc;
    }

    public void setTenKhuVuc(String tenKhuVuc) {
        this.tenKhuVuc = tenKhuVuc;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    public String getTenNguoiGui() {
        return tenNguoiGui;
    }

    public void setTenNguoiGui(String tenNguoiGui) {
        this.tenNguoiGui = tenNguoiGui;
    }

    public LocalDateTime getNgayXacMinh() {
        return ngayXacMinh;
    }

    public void setNgayXacMinh(LocalDateTime ngayXacMinh) {
        this.ngayXacMinh = ngayXacMinh;
    }

    public List<HinhAnh> getDsHinhAnh() {
        return dsHinhAnh;
    }

    public void setDsHinhAnh(List<HinhAnh> dsHinhAnh) {
        this.dsHinhAnh = dsHinhAnh;
    }

    public List<LichSuXacMinh> getDsLichSu() {
        return dsLichSu;
    }

    public void setDsLichSu(List<LichSuXacMinh> dsLichSu) {
        this.dsLichSu = dsLichSu;
    }

    public boolean isDuocSua() {
        return duocSua;
    }

    public void setDuocSua(boolean duocSua) {
        this.duocSua = duocSua;
    }

    public DuBaoNguyCo getDuBao() {
        return duBao;
    }

    public void setDuBao(DuBaoNguyCo duBao) {
        this.duBao = duBao;
    }

    /**
     * Kiểm tra BR01 trên chính báo cáo: đủ trường bắt buộc, 1 đến 5 ảnh, tọa độ đúng miền.
     * Trả về null nếu hợp lệ, ngược lại trả về tên ô sai; câu lỗi lấy bằng {@link #loiHopLe(String)}.
     */
    public String kiemTraHopLe(int soAnh) {
        if (trong(tieuDe)) return "tieuDe";
        if (trong(tenCayTrong)) return "tenCayTrong";
        if (trong(moTa)) return "moTa";
        if (maKhuVuc == null) return "maKhuVuc";
        if (mucDo == null || mucDo < 1 || mucDo > 3) return "mucDo";
        if (ngayPhatHien == null || ngayPhatHien.isAfter(LocalDate.now())) return "ngayPhatHien";
        if (viDo == null || viDo.compareTo(BigDecimal.valueOf(-90)) < 0 || viDo.compareTo(BigDecimal.valueOf(90)) > 0) return "viDo";
        if (kinhDo == null || kinhDo.compareTo(BigDecimal.valueOf(-180)) < 0 || kinhDo.compareTo(BigDecimal.valueOf(180)) > 0) return "kinhDo";
        if (soAnh < 1) return "anhThieu";
        if (soAnh > 5) return "anhThua";
        return null;
    }

    /** Câu báo lỗi tiếng Việt cho mã ô do kiemTraHopLe trả về. */
    public static String loiHopLe(String o) {
        return switch (o) {
            case "tieuDe" -> "Vui lòng nhập tiêu đề.";
            case "tenCayTrong" -> "Vui lòng nhập tên cây trồng.";
            case "moTa" -> "Vui lòng nhập mô tả hiện trường.";
            case "maKhuVuc" -> "Vui lòng chọn khu vực.";
            case "mucDo" -> "Mức độ phải là 1, 2 hoặc 3.";
            case "ngayPhatHien" -> "Ngày phát hiện bắt buộc và không được sau ngày hôm nay.";
            case "viDo" -> "Vĩ độ phải nằm trong khoảng -90 đến 90.";
            case "kinhDo" -> "Kinh độ phải nằm trong khoảng -180 đến 180.";
            case "anhThieu" -> "Báo cáo phải có ít nhất 1 ảnh.";
            case "anhThua" -> "Báo cáo chỉ được có tối đa 5 ảnh.";
            default -> "Dữ liệu không hợp lệ.";
        };
    }

    /**
     * Đổi trạng thái theo bảng chuyển trạng thái (BR03). Chuyển sai thứ tự thì ném lỗi.
     * Cập nhật luôn ngayVaoTrangThai.
     */
    public void chuyenTrangThai(String tt) {
        if (!TrangThai.choPhepChuyen(trangThai, tt)) {
            throw new NghiepVuException(NghiepVuException.Loai.XUNG_DOT,
                    "Không thể chuyển báo cáo từ " + TrangThai.ten(trangThai) + " sang " + TrangThai.ten(tt) + ".", null);
        }
        trangThai = tt;
        ngayVaoTrangThai = LocalDateTime.now();
    }

    /** BR02: báo cáo Chờ xác minh đã quá hanGio giờ kể từ lúc vào trạng thái. */
    public boolean laQuaHan(int hanGio) {
        return TrangThai.CHO_XAC_MINH.equals(trangThai) && ngayVaoTrangThai != null
                && ngayVaoTrangThai.plusHours(hanGio).isBefore(LocalDateTime.now());
    }

    /**
     * BR03, BR04, BR05: chỉ người gửi được sửa; khi còn Chờ xác minh, hoặc Bị bác bỏ mà chưa gửi lại lần nào
     * và chưa quá hanNgay ngày.
     */
    public boolean choPhepSua(int maND, int hanNgay) {
        if (maNguoiGui == null || maNguoiGui != maND) {
            return false;
        }
        if (TrangThai.CHO_XAC_MINH.equals(trangThai)) {
            return true;
        }
        return TrangThai.BI_BAC_BO.equals(trangThai) && soLanGuiLai == 0 && !hetHanGuiLai(hanNgay);
    }

    /** Báo cáo Bị bác bỏ đã quá hanNgay ngày mà chưa gửi lại (dùng cho đóng hồ sơ). */
    public boolean hetHanGuiLai(int hanNgay) {
        return TrangThai.BI_BAC_BO.equals(trangThai) && ngayVaoTrangThai != null
                && ngayVaoTrangThai.plusDays(hanNgay).isBefore(LocalDateTime.now());
    }

    private static boolean trong(String s) {
        return s == null || s.isBlank();
    }
}

package vn.agr05.dal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.agr05.entity.BaoCaoHienTruong;
import vn.agr05.entity.TrangThai;

@Repository
public class BaoCaoDal extends DalCoSo implements IBaoCaoDal {

    private static final String CHON = "SELECT b.MaBaoCao, b.TieuDe, b.TenCayTrong, b.MoTa, b.MucDo, b.ViDo, b.KinhDo, "
            + "b.MaTrangThai, b.KhuyenNghi, b.NgayPhatHien, b.NgayGui, b.NgayVaoTrangThai, b.SoLanGuiLai, b.QuaHan, "
            + "b.MaNguoiGui, b.MaKhuVuc, b.MaLoai, kv.TenKhuVuc, l.TenLoai, nd.HoTen AS TenNguoiGui, "
            + "(SELECT MAX(x.ThoiGian) FROM LichSuXacMinh x WHERE x.MaBaoCao = b.MaBaoCao "
            + "AND x.MaTrangThaiMoi = 'DA_XAC_MINH') AS NgayXacMinh "
            + "FROM BaoCaoHienTruong b JOIN KhuVuc kv ON kv.MaKhuVuc = b.MaKhuVuc "
            + "JOIN NguoiDung nd ON nd.MaNguoiDung = b.MaNguoiGui "
            + "LEFT JOIN LoaiSauBenh l ON l.MaLoai = b.MaLoai ";

    /** Điều kiện "ca đã xác minh": có loại, đang Đã xác minh hoặc Đã xử lý, và có dòng lịch sử sang Đã xác minh. */
    private static final String TU_CA_DA_XAC_MINH = "FROM BaoCaoHienTruong b JOIN LichSuXacMinh x "
            + "ON x.MaBaoCao = b.MaBaoCao AND x.MaTrangThaiMoi = 'DA_XAC_MINH' "
            + "WHERE b.MaLoai IS NOT NULL AND b.MaTrangThai IN ('DA_XAC_MINH', 'DA_XU_LY') ";

    private static final RowMapper<BaoCaoHienTruong> MAP = (rs, i) -> {
        BaoCaoHienTruong bc = new BaoCaoHienTruong();
        bc.setMaBaoCao(rs.getInt("MaBaoCao"));
        bc.setTieuDe(rs.getString("TieuDe"));
        bc.setTenCayTrong(rs.getString("TenCayTrong"));
        bc.setMoTa(rs.getString("MoTa"));
        bc.setMucDo(rs.getInt("MucDo"));
        bc.setViDo(rs.getBigDecimal("ViDo"));
        bc.setKinhDo(rs.getBigDecimal("KinhDo"));
        bc.setTrangThai(rs.getString("MaTrangThai"));
        bc.setKhuyenNghi(rs.getString("KhuyenNghi"));
        bc.setNgayPhatHien(ld(rs, "NgayPhatHien"));
        bc.setNgayGui(ldt(rs, "NgayGui"));
        bc.setNgayVaoTrangThai(ldt(rs, "NgayVaoTrangThai"));
        bc.setSoLanGuiLai(rs.getInt("SoLanGuiLai"));
        bc.setQuaHan(rs.getBoolean("QuaHan"));
        bc.setMaNguoiGui(rs.getInt("MaNguoiGui"));
        bc.setMaKhuVuc(rs.getInt("MaKhuVuc"));
        bc.setMaLoai(intOrNull(rs, "MaLoai"));
        bc.setTenKhuVuc(rs.getString("TenKhuVuc"));
        bc.setTenLoai(rs.getString("TenLoai"));
        bc.setTenNguoiGui(rs.getString("TenNguoiGui"));
        bc.setNgayXacMinh(ldt(rs, "NgayXacMinh"));
        return bc;
    };

    public BaoCaoDal(NamedParameterJdbcTemplate db) {
        super(db);
    }

    /** ngayGui = null thì CSDL tự lấy giờ hiện tại (công cụ dữ liệu demo truyền ngày cũ). */
    @Override
    public int insert(BaoCaoHienTruong bc) {
        return insertLayMa("INSERT INTO BaoCaoHienTruong (TieuDe, TenCayTrong, MoTa, MucDo, ViDo, KinhDo, MaTrangThai, "
                        + "NgayPhatHien, NgayGui, NgayVaoTrangThai, SoLanGuiLai, MaNguoiGui, MaKhuVuc) "
                        + "VALUES (:tieuDe, :cay, :moTa, :mucDo, :viDo, :kinhDo, :tt, :ngayPH, "
                        + "COALESCE(:ngayGui, SYSDATETIME()), COALESCE(:ngayGui, SYSDATETIME()), 0, :nguoi, :kv)",
                thamSo().addValue("tieuDe", bc.getTieuDe()).addValue("cay", bc.getTenCayTrong())
                        .addValue("moTa", bc.getMoTa()).addValue("mucDo", bc.getMucDo())
                        .addValue("viDo", bc.getViDo()).addValue("kinhDo", bc.getKinhDo())
                        .addValue("tt", bc.getTrangThai()).addValue("ngayPH", bc.getNgayPhatHien())
                        .addValue("ngayGui", bc.getNgayGui()).addValue("nguoi", bc.getMaNguoiGui())
                        .addValue("kv", bc.getMaKhuVuc()));
    }

    @Override
    public BaoCaoHienTruong getById(int maBaoCao) {
        List<BaoCaoHienTruong> ds = db.query(CHON + "WHERE b.MaBaoCao = :ma", thamSo().addValue("ma", maBaoCao), MAP);
        return ds.isEmpty() ? null : ds.get(0);
    }

    @Override
    public List<BaoCaoHienTruong> getByNguoiGui(int maNguoiGui) {
        return db.query(CHON + "WHERE b.MaNguoiGui = :ma ORDER BY b.NgayGui DESC", thamSo().addValue("ma", maNguoiGui), MAP);
    }

    @Override
    public List<BaoCaoHienTruong> getByTrangThai(String trangThai, List<Integer> dsMaKhuVuc) {
        MapSqlParameterSource p = thamSo().addValue("tt", trangThai);
        String loc = locKhuVuc("b.MaKhuVuc", dsMaKhuVuc, p);
        return db.query(CHON + "WHERE b.MaTrangThai = :tt" + loc + " ORDER BY b.NgayVaoTrangThai", p, MAP);
    }

    @Override
    public List<BaoCaoHienTruong> getQuaHan(int hanGio) {
        return db.query(CHON + "WHERE b.MaTrangThai = :tt AND b.QuaHan = 0 "
                        + "AND b.NgayVaoTrangThai < DATEADD(HOUR, -:han, SYSDATETIME())",
                thamSo().addValue("tt", TrangThai.CHO_XAC_MINH).addValue("han", hanGio), MAP);
    }

    @Override
    public boolean danhDauQuaHan(int maBaoCao) {
        return db.update("UPDATE BaoCaoHienTruong SET QuaHan = 1 WHERE MaBaoCao = :ma AND QuaHan = 0 AND MaTrangThai = :tt",
                thamSo().addValue("ma", maBaoCao).addValue("tt", TrangThai.CHO_XAC_MINH)) > 0;
    }

    @Override
    public List<BaoCaoHienTruong> getBiBacBoHetHan(int hanNgay) {
        return db.query(CHON + "WHERE b.MaTrangThai = :tt AND b.SoLanGuiLai = 0 "
                        + "AND b.NgayVaoTrangThai < DATEADD(DAY, -:han, SYSDATETIME())",
                thamSo().addValue("tt", TrangThai.BI_BAC_BO).addValue("han", hanNgay), MAP);
    }

    @Override
    public boolean updateTrangThai(BaoCaoHienTruong bc, String trangThaiCu) {
        return db.update("UPDATE BaoCaoHienTruong SET MaTrangThai = :tt, NgayVaoTrangThai = :ngay, MaLoai = :loai, "
                        + "KhuyenNghi = :kn WHERE MaBaoCao = :ma AND MaTrangThai = :ttCu",
                thamSo().addValue("tt", bc.getTrangThai()).addValue("ngay", bc.getNgayVaoTrangThai())
                        .addValue("loai", bc.getMaLoai()).addValue("kn", bc.getKhuyenNghi())
                        .addValue("ma", bc.getMaBaoCao()).addValue("ttCu", trangThaiCu)) > 0;
    }

    @Override
    public boolean updateNoiDung(BaoCaoHienTruong bc, String trangThaiCu) {
        return db.update("UPDATE BaoCaoHienTruong SET TieuDe = :tieuDe, TenCayTrong = :cay, MoTa = :moTa, MucDo = :mucDo, "
                        + "ViDo = :viDo, KinhDo = :kinhDo, NgayPhatHien = :ngayPH, MaKhuVuc = :kv, MaTrangThai = :tt, "
                        + "NgayVaoTrangThai = :ngay, SoLanGuiLai = :soLan, QuaHan = :quaHan "
                        + "WHERE MaBaoCao = :ma AND MaTrangThai = :ttCu",
                thamSo().addValue("tieuDe", bc.getTieuDe()).addValue("cay", bc.getTenCayTrong())
                        .addValue("moTa", bc.getMoTa()).addValue("mucDo", bc.getMucDo())
                        .addValue("viDo", bc.getViDo()).addValue("kinhDo", bc.getKinhDo())
                        .addValue("ngayPH", bc.getNgayPhatHien()).addValue("kv", bc.getMaKhuVuc())
                        .addValue("tt", bc.getTrangThai()).addValue("ngay", bc.getNgayVaoTrangThai())
                        .addValue("soLan", bc.getSoLanGuiLai()).addValue("quaHan", bc.isQuaHan())
                        .addValue("ma", bc.getMaBaoCao()).addValue("ttCu", trangThaiCu)) > 0;
    }

    @Override
    public int demCaDaXacMinh(int maKhuVuc, int maLoai, LocalDateTime tu, LocalDateTime den) {
        Integer n = db.queryForObject("SELECT COUNT(*) " + TU_CA_DA_XAC_MINH
                        + "AND b.MaKhuVuc = :kv AND b.MaLoai = :loai AND x.ThoiGian >= :tu AND x.ThoiGian < :den",
                thamSo().addValue("kv", maKhuVuc).addValue("loai", maLoai).addValue("tu", tu).addValue("den", den),
                Integer.class);
        return n == null ? 0 : n;
    }

    @Override
    public double mucDoTrungBinh(int maKhuVuc, int maLoai, LocalDateTime tu, LocalDateTime den) {
        Double v = db.queryForObject("SELECT AVG(CAST(b.MucDo AS FLOAT)) " + TU_CA_DA_XAC_MINH
                        + "AND b.MaKhuVuc = :kv AND b.MaLoai = :loai AND x.ThoiGian >= :tu AND x.ThoiGian < :den",
                thamSo().addValue("kv", maKhuVuc).addValue("loai", maLoai).addValue("tu", tu).addValue("den", den),
                Double.class);
        return v == null ? 0 : v;
    }

    @Override
    public List<BaoCaoHienTruong> getDaXacMinh() {
        return db.query("SELECT * FROM (" + CHON + "WHERE b.MaLoai IS NOT NULL AND b.MaTrangThai IN ('DA_XAC_MINH', 'DA_XU_LY')"
                + ") t WHERE t.NgayXacMinh IS NOT NULL ORDER BY t.NgayXacMinh", MAP);
    }

    @Override
    public LinkedHashMap<String, Integer> demTheoTrangThai(LocalDate tu, LocalDate den, List<Integer> dsMaKhuVuc,
                                                           Integer maLoai) {
        MapSqlParameterSource p = thamSo().addValue("tu", tu).addValue("den", den.plusDays(1));
        String loc = locKhuVuc("b.MaKhuVuc", dsMaKhuVuc, p) + locLoai(maLoai, p);
        LinkedHashMap<String, Integer> kq = new LinkedHashMap<>();
        db.query("SELECT t.MaTrangThai, COUNT(b.MaBaoCao) AS SoLuong FROM TrangThaiBaoCao t "
                + "LEFT JOIN BaoCaoHienTruong b ON b.MaTrangThai = t.MaTrangThai AND b.NgayGui >= :tu AND b.NgayGui < :den"
                + loc + " GROUP BY t.MaTrangThai, t.ThuTu ORDER BY t.ThuTu", p,
                rs -> {
                    kq.put(rs.getString("MaTrangThai"), rs.getInt("SoLuong"));
                });
        return kq;
    }

    @Override
    public LinkedHashMap<String, Integer> demTheoTuan(LocalDate tu, LocalDate den, List<Integer> dsMaKhuVuc,
                                                      Integer maLoai) {
        MapSqlParameterSource p = thamSo().addValue("tu", tu).addValue("den", den.plusDays(1));
        String loc = locKhuVuc("b.MaKhuVuc", dsMaKhuVuc, p) + locLoai(maLoai, p);
        LinkedHashMap<String, Integer> kq = new LinkedHashMap<>();
        // Ngày đầu tuần = thứ Hai (DATEDIFF tính từ 1900-01-01 là thứ Hai)
        db.query("SELECT CONVERT(CHAR(10), DATEADD(DAY, (DATEDIFF(DAY, '19000101', x.ThoiGian) / 7) * 7, '19000101'), 23) "
                + "AS Tuan, COUNT(*) AS SoCa " + TU_CA_DA_XAC_MINH + "AND x.ThoiGian >= :tu AND x.ThoiGian < :den" + loc
                + " GROUP BY DATEDIFF(DAY, '19000101', x.ThoiGian) / 7 ORDER BY 1", p,
                rs -> {
                    kq.put(rs.getString("Tuan"), rs.getInt("SoCa"));
                });
        return kq;
    }

    private static String locLoai(Integer maLoai, MapSqlParameterSource p) {
        if (maLoai == null) {
            return "";
        }
        p.addValue("loai", maLoai);
        return " AND b.MaLoai = :loai";
    }
}

package vn.agr05.dal;

import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.agr05.entity.NguoiDung;
import vn.agr05.entity.VaiTro;

@Repository
public class NguoiDungDal extends DalCoSo implements INguoiDungDal {

    private static final String CHON = "SELECT MaNguoiDung, TenDangNhap, MatKhauHash, Muoi, HoTen, DienThoai, "
            + "MaVaiTro, BiKhoa, NgayTao FROM NguoiDung ";

    private static final RowMapper<NguoiDung> MAP = (rs, i) -> {
        NguoiDung nd = new NguoiDung();
        nd.setMaNguoiDung(rs.getInt("MaNguoiDung"));
        nd.setTenDangNhap(rs.getString("TenDangNhap"));
        nd.setMatKhauHash(rs.getString("MatKhauHash"));
        nd.setMuoi(rs.getString("Muoi"));
        nd.setHoTen(rs.getString("HoTen"));
        nd.setDienThoai(rs.getString("DienThoai"));
        nd.setVaiTro(rs.getString("MaVaiTro"));
        nd.setBiKhoa(rs.getBoolean("BiKhoa"));
        nd.setNgayTao(ldt(rs, "NgayTao"));
        return nd;
    };

    public NguoiDungDal(NamedParameterJdbcTemplate db) {
        super(db);
    }

    @Override
    public NguoiDung getByTenDangNhap(String tenDangNhap) {
        List<NguoiDung> ds = db.query(CHON + "WHERE TenDangNhap = :ten", thamSo().addValue("ten", tenDangNhap), MAP);
        return ds.isEmpty() ? null : ds.get(0);
    }

    @Override
    public NguoiDung getById(int maNguoiDung) {
        List<NguoiDung> ds = db.query(CHON + "WHERE MaNguoiDung = :ma", thamSo().addValue("ma", maNguoiDung), MAP);
        return ds.isEmpty() ? null : ds.get(0);
    }

    @Override
    public List<NguoiDung> getAll() {
        return db.query(CHON + "ORDER BY MaVaiTro, TenDangNhap", MAP);
    }

    @Override
    public int insert(NguoiDung nd) {
        return insertLayMa("INSERT INTO NguoiDung (TenDangNhap, MatKhauHash, Muoi, HoTen, DienThoai, MaVaiTro, BiKhoa) "
                + "VALUES (:ten, :hash, :muoi, :hoTen, :dt, :vaiTro, :khoa)",
                thamSo().addValue("ten", nd.getTenDangNhap()).addValue("hash", nd.getMatKhauHash())
                        .addValue("muoi", nd.getMuoi()).addValue("hoTen", nd.getHoTen())
                        .addValue("dt", nd.getDienThoai()).addValue("vaiTro", nd.getVaiTro())
                        .addValue("khoa", nd.isBiKhoa()));
    }

    @Override
    public boolean update(NguoiDung nd) {
        return db.update("UPDATE NguoiDung SET HoTen = :hoTen, DienThoai = :dt, MaVaiTro = :vaiTro WHERE MaNguoiDung = :ma",
                thamSo().addValue("hoTen", nd.getHoTen()).addValue("dt", nd.getDienThoai())
                        .addValue("vaiTro", nd.getVaiTro()).addValue("ma", nd.getMaNguoiDung())) > 0;
    }

    @Override
    public boolean updateBiKhoa(int maNguoiDung, boolean khoa) {
        return db.update("UPDATE NguoiDung SET BiKhoa = :khoa WHERE MaNguoiDung = :ma",
                thamSo().addValue("khoa", khoa).addValue("ma", maNguoiDung)) > 0;
    }

    @Override
    public List<Integer> getKhuVucPhuTrach(int maNguoiDung) {
        return db.queryForList("SELECT MaKhuVuc FROM PhanCongKhuVuc WHERE MaNguoiDung = :ma ORDER BY MaKhuVuc",
                thamSo().addValue("ma", maNguoiDung), Integer.class);
    }

    @Override
    public List<NguoiDung> getChuyenGiaTheoKhuVuc(int maKhuVuc) {
        return db.query(CHON + "WHERE BiKhoa = 0 AND MaVaiTro = :vt AND MaNguoiDung IN "
                        + "(SELECT MaNguoiDung FROM PhanCongKhuVuc WHERE MaKhuVuc = :kv)",
                thamSo().addValue("vt", VaiTro.CHUYEN_GIA).addValue("kv", maKhuVuc), MAP);
    }

    @Override
    public List<NguoiDung> getNongDanTheoKhuVuc(int maKhuVuc) {
        return db.query(CHON + "WHERE BiKhoa = 0 AND MaVaiTro = :vt AND MaNguoiDung IN "
                        + "(SELECT MaNguoiGui FROM BaoCaoHienTruong WHERE MaKhuVuc = :kv)",
                thamSo().addValue("vt", VaiTro.NONG_DAN).addValue("kv", maKhuVuc), MAP);
    }

    @Override
    public List<NguoiDung> getQuanTri() {
        return db.query(CHON + "WHERE BiKhoa = 0 AND MaVaiTro = :vt", thamSo().addValue("vt", VaiTro.QUAN_TRI), MAP);
    }
}

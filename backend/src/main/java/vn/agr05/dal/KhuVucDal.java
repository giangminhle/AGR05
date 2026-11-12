package vn.agr05.dal;

import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.agr05.entity.KhuVuc;
import vn.agr05.entity.NguoiDung;

@Repository
public class KhuVucDal extends DalCoSo implements IKhuVucDal {

    private static final String CHON = "SELECT k.MaKhuVuc, k.TenKhuVuc, k.MoTa, "
            + "(SELECT COUNT(*) FROM PhanCongKhuVuc p WHERE p.MaKhuVuc = k.MaKhuVuc) AS SoChuyenGia FROM KhuVuc k ";

    private static final RowMapper<KhuVuc> MAP = (rs, i) -> {
        KhuVuc kv = new KhuVuc();
        kv.setMaKhuVuc(rs.getInt("MaKhuVuc"));
        kv.setTenKhuVuc(rs.getString("TenKhuVuc"));
        kv.setMoTa(rs.getString("MoTa"));
        kv.setSoChuyenGia(rs.getInt("SoChuyenGia"));
        return kv;
    };

    public KhuVucDal(NamedParameterJdbcTemplate db) {
        super(db);
    }

    @Override
    public List<KhuVuc> getAll() {
        return db.query(CHON + "ORDER BY k.TenKhuVuc", MAP);
    }

    @Override
    public KhuVuc getById(int maKhuVuc) {
        List<KhuVuc> ds = db.query(CHON + "WHERE k.MaKhuVuc = :ma", thamSo().addValue("ma", maKhuVuc), MAP);
        return ds.isEmpty() ? null : ds.get(0);
    }

    @Override
    public int insert(KhuVuc kv) {
        return insertLayMa("INSERT INTO KhuVuc (TenKhuVuc, MoTa) VALUES (:ten, :moTa)",
                thamSo().addValue("ten", kv.getTenKhuVuc()).addValue("moTa", kv.getMoTa()));
    }

    @Override
    public boolean update(KhuVuc kv) {
        return db.update("UPDATE KhuVuc SET TenKhuVuc = :ten, MoTa = :moTa WHERE MaKhuVuc = :ma",
                thamSo().addValue("ten", kv.getTenKhuVuc()).addValue("moTa", kv.getMoTa())
                        .addValue("ma", kv.getMaKhuVuc())) > 0;
    }

    @Override
    public boolean coPhanCong(int maChuyenGia, int maKhuVuc) {
        Integer n = db.queryForObject("SELECT COUNT(*) FROM PhanCongKhuVuc WHERE MaNguoiDung = :cg AND MaKhuVuc = :kv",
                thamSo().addValue("cg", maChuyenGia).addValue("kv", maKhuVuc), Integer.class);
        return n != null && n > 0;
    }

    @Override
    public void insertPhanCong(int maChuyenGia, int maKhuVuc) {
        db.update("INSERT INTO PhanCongKhuVuc (MaNguoiDung, MaKhuVuc) VALUES (:cg, :kv)",
                thamSo().addValue("cg", maChuyenGia).addValue("kv", maKhuVuc));
    }

    @Override
    public boolean deletePhanCong(int maChuyenGia, int maKhuVuc) {
        return db.update("DELETE FROM PhanCongKhuVuc WHERE MaNguoiDung = :cg AND MaKhuVuc = :kv",
                thamSo().addValue("cg", maChuyenGia).addValue("kv", maKhuVuc)) > 0;
    }

    @Override
    public List<NguoiDung> getChuyenGia(int maKhuVuc) {
        return db.query("SELECT n.MaNguoiDung, n.TenDangNhap, n.HoTen, n.DienThoai, n.MaVaiTro, n.BiKhoa "
                        + "FROM NguoiDung n JOIN PhanCongKhuVuc p ON p.MaNguoiDung = n.MaNguoiDung "
                        + "WHERE p.MaKhuVuc = :kv ORDER BY n.HoTen",
                thamSo().addValue("kv", maKhuVuc), (rs, i) -> {
                    NguoiDung nd = new NguoiDung();
                    nd.setMaNguoiDung(rs.getInt("MaNguoiDung"));
                    nd.setTenDangNhap(rs.getString("TenDangNhap"));
                    nd.setHoTen(rs.getString("HoTen"));
                    nd.setDienThoai(rs.getString("DienThoai"));
                    nd.setVaiTro(rs.getString("MaVaiTro"));
                    nd.setBiKhoa(rs.getBoolean("BiKhoa"));
                    return nd;
                });
    }
}

package vn.agr05.dal;

import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.agr05.entity.LoaiSauBenh;

@Repository
public class LoaiSauBenhDal extends DalCoSo implements ILoaiSauBenhDal {

    private static final String CHON = "SELECT MaLoai, TenLoai, TrieuChung, BienPhapPhongTru FROM LoaiSauBenh ";

    private static final RowMapper<LoaiSauBenh> MAP = (rs, i) -> {
        LoaiSauBenh l = new LoaiSauBenh();
        l.setMaLoai(rs.getInt("MaLoai"));
        l.setTenLoai(rs.getString("TenLoai"));
        l.setTrieuChung(rs.getString("TrieuChung"));
        l.setBienPhapPhongTru(rs.getString("BienPhapPhongTru"));
        return l;
    };

    public LoaiSauBenhDal(NamedParameterJdbcTemplate db) {
        super(db);
    }

    @Override
    public List<LoaiSauBenh> getAll() {
        return db.query(CHON + "ORDER BY TenLoai", MAP);
    }

    @Override
    public LoaiSauBenh getById(int maLoai) {
        List<LoaiSauBenh> ds = db.query(CHON + "WHERE MaLoai = :ma", thamSo().addValue("ma", maLoai), MAP);
        return ds.isEmpty() ? null : ds.get(0);
    }

    @Override
    public int insert(LoaiSauBenh loai) {
        return insertLayMa("INSERT INTO LoaiSauBenh (TenLoai, TrieuChung, BienPhapPhongTru) VALUES (:ten, :tc, :bp)",
                thamSo().addValue("ten", loai.getTenLoai()).addValue("tc", loai.getTrieuChung())
                        .addValue("bp", loai.getBienPhapPhongTru()));
    }

    @Override
    public boolean update(LoaiSauBenh loai) {
        return db.update("UPDATE LoaiSauBenh SET TenLoai = :ten, TrieuChung = :tc, BienPhapPhongTru = :bp WHERE MaLoai = :ma",
                thamSo().addValue("ten", loai.getTenLoai()).addValue("tc", loai.getTrieuChung())
                        .addValue("bp", loai.getBienPhapPhongTru()).addValue("ma", loai.getMaLoai())) > 0;
    }

    @Override
    public boolean dangDuocDung(int maLoai) {
        Integer n = db.queryForObject("SELECT (SELECT COUNT(*) FROM BaoCaoHienTruong WHERE MaLoai = :ma) "
                + "+ (SELECT COUNT(*) FROM CanhBao WHERE MaLoai = :ma)", thamSo().addValue("ma", maLoai), Integer.class);
        return n != null && n > 0;
    }

    @Override
    public boolean delete(int maLoai) {
        return db.update("DELETE FROM LoaiSauBenh WHERE MaLoai = :ma", thamSo().addValue("ma", maLoai)) > 0;
    }
}

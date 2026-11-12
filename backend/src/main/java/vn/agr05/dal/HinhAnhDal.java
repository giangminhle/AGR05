package vn.agr05.dal;

import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.agr05.entity.HinhAnh;

@Repository
public class HinhAnhDal extends DalCoSo implements IHinhAnhDal {

    private static final String CHON = "SELECT MaHinhAnh, MaBaoCao, DuongDan, NgayTai FROM HinhAnh ";

    private static final RowMapper<HinhAnh> MAP = (rs, i) -> {
        HinhAnh ha = new HinhAnh();
        ha.setMaHinhAnh(rs.getInt("MaHinhAnh"));
        ha.setMaBaoCao(rs.getInt("MaBaoCao"));
        ha.setDuongDan(rs.getString("DuongDan"));
        ha.setNgayTai(ldt(rs, "NgayTai"));
        return ha;
    };

    public HinhAnhDal(NamedParameterJdbcTemplate db) {
        super(db);
    }

    @Override
    public List<HinhAnh> getByBaoCao(int maBaoCao) {
        return db.query(CHON + "WHERE MaBaoCao = :ma ORDER BY MaHinhAnh", thamSo().addValue("ma", maBaoCao), MAP);
    }

    @Override
    public HinhAnh getById(int maHinhAnh) {
        List<HinhAnh> ds = db.query(CHON + "WHERE MaHinhAnh = :ma", thamSo().addValue("ma", maHinhAnh), MAP);
        return ds.isEmpty() ? null : ds.get(0);
    }

    @Override
    public int insert(HinhAnh ha) {
        return insertLayMa("INSERT INTO HinhAnh (MaBaoCao, DuongDan) VALUES (:bc, :dd)",
                thamSo().addValue("bc", ha.getMaBaoCao()).addValue("dd", ha.getDuongDan()));
    }

    @Override
    public boolean delete(int maHinhAnh) {
        return db.update("DELETE FROM HinhAnh WHERE MaHinhAnh = :ma", thamSo().addValue("ma", maHinhAnh)) > 0;
    }
}

package vn.agr05.dal;

import java.util.List;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.agr05.entity.ThamSoHeThong;

@Repository
public class ThamSoDal extends DalCoSo implements IThamSoDal {

    public ThamSoDal(NamedParameterJdbcTemplate db) {
        super(db);
    }

    @Override
    public List<ThamSoHeThong> getAll() {
        return db.query("SELECT MaThamSo, GiaTri, MoTa FROM ThamSoHeThong ORDER BY MaThamSo", (rs, i) -> {
            ThamSoHeThong ts = new ThamSoHeThong();
            ts.setMaThamSo(rs.getString("MaThamSo"));
            ts.setGiaTri(rs.getInt("GiaTri"));
            ts.setMoTa(rs.getString("MoTa"));
            return ts;
        });
    }

    @Override
    public Integer getGiaTri(String maThamSo) {
        List<Integer> ds = db.queryForList("SELECT GiaTri FROM ThamSoHeThong WHERE MaThamSo = :ma",
                thamSo().addValue("ma", maThamSo), Integer.class);
        return ds.isEmpty() ? null : ds.get(0);
    }

    @Override
    public boolean update(String maThamSo, int giaTri) {
        return db.update("UPDATE ThamSoHeThong SET GiaTri = :gt WHERE MaThamSo = :ma",
                thamSo().addValue("gt", giaTri).addValue("ma", maThamSo)) > 0;
    }
}

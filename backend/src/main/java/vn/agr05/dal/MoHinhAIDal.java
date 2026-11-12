package vn.agr05.dal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.agr05.entity.MoHinhAI;

@Repository
public class MoHinhAIDal extends DalCoSo implements IMoHinhAIDal {

    private static final String CHON = "SELECT MaMoHinh, PhienBan, DuongDanFile, NgayHuanLuyen, SoMauHuanLuyen, "
            + "Accuracy, F1, AUC, DangSuDung FROM MoHinhAI ";

    private static final RowMapper<MoHinhAI> MAP = (rs, i) -> {
        MoHinhAI mh = new MoHinhAI();
        mh.setMaMoHinh(rs.getInt("MaMoHinh"));
        mh.setPhienBan(rs.getString("PhienBan"));
        mh.setDuongDanFile(rs.getString("DuongDanFile"));
        mh.setNgayHuanLuyen(ldt(rs, "NgayHuanLuyen"));
        mh.setSoMauHuanLuyen(rs.getInt("SoMauHuanLuyen"));
        mh.setAccuracy(rs.getDouble("Accuracy"));
        mh.setF1(rs.getDouble("F1"));
        mh.setAuc(rs.getDouble("AUC"));
        mh.setDangSuDung(rs.getBoolean("DangSuDung"));
        return mh;
    };

    public MoHinhAIDal(NamedParameterJdbcTemplate db) {
        super(db);
    }

    @Override
    public List<MoHinhAI> getAll() {
        return db.query(CHON + "ORDER BY NgayHuanLuyen DESC, MaMoHinh DESC", MAP);
    }

    @Override
    public MoHinhAI getById(int maMoHinh) {
        List<MoHinhAI> ds = db.query(CHON + "WHERE MaMoHinh = :ma", thamSo().addValue("ma", maMoHinh), MAP);
        return ds.isEmpty() ? null : ds.get(0);
    }

    @Override
    public MoHinhAI getDangSuDung() {
        List<MoHinhAI> ds = db.query(CHON + "WHERE DangSuDung = 1", MAP);
        return ds.isEmpty() ? null : ds.get(0);
    }

    @Override
    public int insert(MoHinhAI mh) {
        return insertLayMa("INSERT INTO MoHinhAI (PhienBan, DuongDanFile, SoMauHuanLuyen, Accuracy, F1, AUC, DangSuDung) "
                        + "VALUES (:pb, :file, :soMau, :acc, :f1, :auc, 0)",
                thamSo().addValue("pb", mh.getPhienBan()).addValue("file", mh.getDuongDanFile())
                        .addValue("soMau", mh.getSoMauHuanLuyen()).addValue("acc", bon(mh.getAccuracy()))
                        .addValue("f1", bon(mh.getF1())).addValue("auc", bon(mh.getAuc())));
    }

    @Override
    public void datDangSuDung(int maMoHinh) {
        db.update("UPDATE MoHinhAI SET DangSuDung = 0 WHERE DangSuDung = 1", thamSo());
        db.update("UPDATE MoHinhAI SET DangSuDung = 1 WHERE MaMoHinh = :ma", thamSo().addValue("ma", maMoHinh));
    }

    /** Làm tròn 4 chữ số cho cột DECIMAL(5,4). */
    private static BigDecimal bon(double v) {
        return BigDecimal.valueOf(v).setScale(4, RoundingMode.HALF_UP);
    }
}

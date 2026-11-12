package vn.agr05.dal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * Lớp cha của các DAL: giữ NamedParameterJdbcTemplate và vài hàm đọc cột.
 * JdbcTemplate tự dùng kết nối của transaction đang mở (do Service mở bằng TransactionTemplate),
 * nên các hàm DAL không nhận tham số transaction.
 */
public abstract class DalCoSo {

    protected final NamedParameterJdbcTemplate db;

    protected DalCoSo(NamedParameterJdbcTemplate db) {
        this.db = db;
    }

    /** Chạy câu INSERT có tham số và trả về khóa IDENTITY vừa sinh. */
    protected int insertLayMa(String sql, MapSqlParameterSource p) {
        KeyHolder kh = new GeneratedKeyHolder();
        db.update(sql, p, kh);
        Number ma = kh.getKey();
        if (ma == null) {
            throw new IllegalStateException("Không lấy được mã vừa thêm");
        }
        return ma.intValue();
    }

    /**
     * Thêm điều kiện lọc khu vực. dsMaKhuVuc = null nghĩa là không lọc (Quản trị viên).
     * Danh sách rỗng thì điều kiện luôn sai.
     */
    protected static String locKhuVuc(String cot, List<Integer> dsMaKhuVuc, MapSqlParameterSource p) {
        if (dsMaKhuVuc == null) {
            return "";
        }
        if (dsMaKhuVuc.isEmpty()) {
            return " AND 1 = 0";
        }
        p.addValue("dsKV", dsMaKhuVuc);
        return " AND " + cot + " IN (:dsKV)";
    }

    protected static LocalDateTime ldt(ResultSet rs, String cot) throws SQLException {
        return rs.getObject(cot, LocalDateTime.class);
    }

    protected static LocalDate ld(ResultSet rs, String cot) throws SQLException {
        return rs.getObject(cot, LocalDate.class);
    }

    protected static Integer intOrNull(ResultSet rs, String cot) throws SQLException {
        int v = rs.getInt(cot);
        return rs.wasNull() ? null : v;
    }

    protected static MapSqlParameterSource thamSo() {
        return new MapSqlParameterSource();
    }
}

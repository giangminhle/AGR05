package vn.agr05.dal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.agr05.entity.CanhBao;

@Repository
public class CanhBaoDal extends DalCoSo implements ICanhBaoDal {

    public CanhBaoDal(NamedParameterJdbcTemplate db) {
        super(db);
    }

    @Override
    public int insert(CanhBao cb) {
        return insertLayMa("INSERT INTO CanhBao (MaKhuVuc, MaLoai, MaDuBao, NoiDung) VALUES (:kv, :loai, :db, :nd)",
                thamSo().addValue("kv", cb.getMaKhuVuc()).addValue("loai", cb.getMaLoai())
                        .addValue("db", cb.getMaDuBao()).addValue("nd", cb.getNoiDung()));
    }

    @Override
    public boolean coCanhBaoTu(int maKhuVuc, int maLoai, LocalDateTime tu) {
        Integer n = db.queryForObject("SELECT COUNT(*) FROM CanhBao WHERE MaKhuVuc = :kv AND MaLoai = :loai AND NgayTao >= :tu",
                thamSo().addValue("kv", maKhuVuc).addValue("loai", maLoai).addValue("tu", tu), Integer.class);
        return n != null && n > 0;
    }

    @Override
    public List<CanhBao> getTrongKy(LocalDate tu, LocalDate den, List<Integer> dsMaKhuVuc, Integer maLoai) {
        MapSqlParameterSource p = thamSo().addValue("tu", tu).addValue("den", den.plusDays(1));
        String loc = locKhuVuc("c.MaKhuVuc", dsMaKhuVuc, p);
        if (maLoai != null) {
            p.addValue("loai", maLoai);
            loc += " AND c.MaLoai = :loai";
        }
        return db.query("SELECT c.MaCanhBao, c.MaKhuVuc, c.MaLoai, c.MaDuBao, c.NoiDung, c.NgayTao, k.TenKhuVuc, l.TenLoai "
                        + "FROM CanhBao c JOIN KhuVuc k ON k.MaKhuVuc = c.MaKhuVuc JOIN LoaiSauBenh l ON l.MaLoai = c.MaLoai "
                        + "WHERE c.NgayTao >= :tu AND c.NgayTao < :den" + loc + " ORDER BY c.NgayTao DESC",
                p, (rs, i) -> {
                    CanhBao cb = new CanhBao();
                    cb.setMaCanhBao(rs.getInt("MaCanhBao"));
                    cb.setMaKhuVuc(rs.getInt("MaKhuVuc"));
                    cb.setMaLoai(rs.getInt("MaLoai"));
                    cb.setMaDuBao(intOrNull(rs, "MaDuBao"));
                    cb.setNoiDung(rs.getString("NoiDung"));
                    cb.setNgayTao(ldt(rs, "NgayTao"));
                    cb.setTenKhuVuc(rs.getString("TenKhuVuc"));
                    cb.setTenLoai(rs.getString("TenLoai"));
                    return cb;
                });
    }
}

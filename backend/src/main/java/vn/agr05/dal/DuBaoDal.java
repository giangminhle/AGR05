package vn.agr05.dal;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.agr05.entity.DuBaoNguyCo;
import vn.agr05.entity.MucNguyCo;

@Repository
public class DuBaoDal extends DalCoSo implements IDuBaoDal {

    public DuBaoDal(NamedParameterJdbcTemplate db) {
        super(db);
    }

    @Override
    public int insert(DuBaoNguyCo d) {
        return insertLayMa("INSERT INTO DuBaoNguyCo (MaBaoCao, MaMoHinh, XacSuat, MucNguyCo, SoNgayDuBao) "
                        + "VALUES (:bc, :mh, :xs, :muc, :soNgay)",
                thamSo().addValue("bc", d.getMaBaoCao()).addValue("mh", d.getMaMoHinh())
                        .addValue("xs", Math.round(d.getXacSuat() * 10000) / 10000.0)
                        .addValue("muc", d.getMucNguyCo()).addValue("soNgay", d.getSoNgayDuBao()));
    }

    @Override
    public DuBaoNguyCo getByBaoCao(int maBaoCao) {
        List<DuBaoNguyCo> ds = db.query("SELECT MaDuBao, MaBaoCao, MaMoHinh, XacSuat, MucNguyCo, SoNgayDuBao, NgayDuBao "
                + "FROM DuBaoNguyCo WHERE MaBaoCao = :ma", thamSo().addValue("ma", maBaoCao), (rs, i) -> {
                    DuBaoNguyCo d = new DuBaoNguyCo();
                    d.setMaDuBao(rs.getInt("MaDuBao"));
                    d.setMaBaoCao(rs.getInt("MaBaoCao"));
                    d.setMaMoHinh(rs.getInt("MaMoHinh"));
                    d.setXacSuat(rs.getDouble("XacSuat"));
                    d.setMucNguyCo(rs.getString("MucNguyCo"));
                    d.setSoNgayDuBao(rs.getInt("SoNgayDuBao"));
                    d.setNgayDuBao(ldt(rs, "NgayDuBao"));
                    return d;
                });
        return ds.isEmpty() ? null : ds.get(0);
    }

    @Override
    public LinkedHashMap<String, Integer> demTheoMuc(LocalDate tu, LocalDate den, List<Integer> dsMaKhuVuc,
                                                     Integer maLoai) {
        MapSqlParameterSource p = thamSo().addValue("tu", tu).addValue("den", den.plusDays(1));
        String loc = locKhuVuc("b.MaKhuVuc", dsMaKhuVuc, p);
        if (maLoai != null) {
            p.addValue("loai", maLoai);
            loc += " AND b.MaLoai = :loai";
        }
        LinkedHashMap<String, Integer> kq = new LinkedHashMap<>();
        kq.put(MucNguyCo.THAP, 0);
        kq.put(MucNguyCo.TRUNG_BINH, 0);
        kq.put(MucNguyCo.CAO, 0);
        db.query("SELECT d.MucNguyCo, COUNT(*) AS SoLuong FROM DuBaoNguyCo d JOIN BaoCaoHienTruong b ON b.MaBaoCao = d.MaBaoCao "
                + "WHERE d.NgayDuBao >= :tu AND d.NgayDuBao < :den" + loc + " GROUP BY d.MucNguyCo", p,
                rs -> {
                    kq.put(rs.getString("MucNguyCo"), rs.getInt("SoLuong"));
                });
        return kq;
    }
}

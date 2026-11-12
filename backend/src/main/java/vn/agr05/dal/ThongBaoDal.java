package vn.agr05.dal;

import java.util.List;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.agr05.entity.ThongBao;

@Repository
public class ThongBaoDal extends DalCoSo implements IThongBaoDal {

    public ThongBaoDal(NamedParameterJdbcTemplate db) {
        super(db);
    }

    @Override
    public int insert(ThongBao tb) {
        return insertLayMa("INSERT INTO ThongBao (MaNguoiNhan, MaBaoCao, MaLoaiTB, TieuDe, NoiDung) "
                        + "VALUES (:nhan, :bc, :loai, :tieuDe, :noiDung)",
                thamSo().addValue("nhan", tb.getMaNguoiNhan()).addValue("bc", tb.getMaBaoCao())
                        .addValue("loai", tb.getLoai()).addValue("tieuDe", tb.getTieuDe())
                        .addValue("noiDung", tb.getNoiDung()));
    }

    @Override
    public int countChuaDoc(int maNguoiNhan) {
        Integer n = db.queryForObject("SELECT COUNT(*) FROM ThongBao WHERE MaNguoiNhan = :ma AND DaDoc = 0",
                thamSo().addValue("ma", maNguoiNhan), Integer.class);
        return n == null ? 0 : n;
    }

    @Override
    public List<ThongBao> getByNguoiNhan(int maNguoiNhan) {
        return db.query("SELECT MaThongBao, MaNguoiNhan, MaBaoCao, MaLoaiTB, TieuDe, NoiDung, NgayTao, DaDoc "
                        + "FROM ThongBao WHERE MaNguoiNhan = :ma ORDER BY NgayTao DESC, MaThongBao DESC",
                thamSo().addValue("ma", maNguoiNhan), (rs, i) -> {
                    ThongBao tb = new ThongBao();
                    tb.setMaThongBao(rs.getInt("MaThongBao"));
                    tb.setMaNguoiNhan(rs.getInt("MaNguoiNhan"));
                    tb.setMaBaoCao(intOrNull(rs, "MaBaoCao"));
                    tb.setLoai(rs.getString("MaLoaiTB"));
                    tb.setTieuDe(rs.getString("TieuDe"));
                    tb.setNoiDung(rs.getString("NoiDung"));
                    tb.setNgayTao(ldt(rs, "NgayTao"));
                    tb.setDaDoc(rs.getBoolean("DaDoc"));
                    return tb;
                });
    }

    @Override
    public boolean markRead(int maThongBao, int maNguoiNhan) {
        return db.update("UPDATE ThongBao SET DaDoc = 1 WHERE MaThongBao = :ma AND MaNguoiNhan = :nhan",
                thamSo().addValue("ma", maThongBao).addValue("nhan", maNguoiNhan)) > 0;
    }
}

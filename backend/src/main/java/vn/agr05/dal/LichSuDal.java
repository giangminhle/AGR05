package vn.agr05.dal;

import java.util.List;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.agr05.entity.LichSuXacMinh;

@Repository
public class LichSuDal extends DalCoSo implements ILichSuDal {

    public LichSuDal(NamedParameterJdbcTemplate db) {
        super(db);
    }

    @Override
    public List<LichSuXacMinh> getByBaoCao(int maBaoCao) {
        return db.query("SELECT l.MaLichSu, l.MaBaoCao, l.MaNguoiThucHien, l.MaTrangThaiCu, l.MaTrangThaiMoi, "
                        + "l.GhiChu, l.ThoiGian, n.HoTen FROM LichSuXacMinh l "
                        + "LEFT JOIN NguoiDung n ON n.MaNguoiDung = l.MaNguoiThucHien "
                        + "WHERE l.MaBaoCao = :ma ORDER BY l.ThoiGian, l.MaLichSu",
                thamSo().addValue("ma", maBaoCao), (rs, i) -> {
                    LichSuXacMinh ls = new LichSuXacMinh();
                    ls.setMaLichSu(rs.getInt("MaLichSu"));
                    ls.setMaBaoCao(rs.getInt("MaBaoCao"));
                    ls.setMaNguoiThucHien(intOrNull(rs, "MaNguoiThucHien"));
                    ls.setTrangThaiCu(rs.getString("MaTrangThaiCu"));
                    ls.setTrangThaiMoi(rs.getString("MaTrangThaiMoi"));
                    ls.setGhiChu(rs.getString("GhiChu"));
                    ls.setThoiGian(ldt(rs, "ThoiGian"));
                    String ten = rs.getString("HoTen");
                    ls.setTenNguoiThucHien(ten == null ? "Hệ thống" : ten);
                    return ls;
                });
    }

    /** thoiGian = null thì lấy giờ hiện tại của CSDL. */
    @Override
    public int insert(LichSuXacMinh ls) {
        return insertLayMa("INSERT INTO LichSuXacMinh (MaBaoCao, MaNguoiThucHien, MaTrangThaiCu, MaTrangThaiMoi, GhiChu, ThoiGian) "
                        + "VALUES (:bc, :nguoi, :cu, :moi, :ghiChu, COALESCE(:tg, SYSDATETIME()))",
                thamSo().addValue("bc", ls.getMaBaoCao()).addValue("nguoi", ls.getMaNguoiThucHien())
                        .addValue("cu", ls.getTrangThaiCu()).addValue("moi", ls.getTrangThaiMoi())
                        .addValue("ghiChu", ls.getGhiChu()).addValue("tg", ls.getThoiGian()));
    }
}

package vn.agr05.fake;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import vn.agr05.dal.IBaoCaoDal;
import vn.agr05.entity.BaoCaoHienTruong;

/** Bọc một IBaoCaoDal để test ghi đè từng hàm. */
public class BaoCaoDalUyQuyen implements IBaoCaoDal {

    private final IBaoCaoDal goc;

    public BaoCaoDalUyQuyen(IBaoCaoDal goc) {
        this.goc = goc;
    }

    public int insert(BaoCaoHienTruong bc) {
        return goc.insert(bc);
    }

    public BaoCaoHienTruong getById(int m) {
        return goc.getById(m);
    }

    public List<BaoCaoHienTruong> getByNguoiGui(int m) {
        return goc.getByNguoiGui(m);
    }

    public List<BaoCaoHienTruong> getByTrangThai(String tt, List<Integer> ds) {
        return goc.getByTrangThai(tt, ds);
    }

    public List<BaoCaoHienTruong> getQuaHan(int h) {
        return goc.getQuaHan(h);
    }

    public boolean danhDauQuaHan(int m) {
        return goc.danhDauQuaHan(m);
    }

    public List<BaoCaoHienTruong> getBiBacBoHetHan(int h) {
        return goc.getBiBacBoHetHan(h);
    }

    public boolean updateTrangThai(BaoCaoHienTruong bc, String tt) {
        return goc.updateTrangThai(bc, tt);
    }

    public boolean updateNoiDung(BaoCaoHienTruong bc, String tt) {
        return goc.updateNoiDung(bc, tt);
    }

    public int demCaDaXacMinh(int kv, int l, LocalDateTime tu, LocalDateTime den) {
        return goc.demCaDaXacMinh(kv, l, tu, den);
    }

    public double mucDoTrungBinh(int kv, int l, LocalDateTime tu, LocalDateTime den) {
        return goc.mucDoTrungBinh(kv, l, tu, den);
    }

    public List<BaoCaoHienTruong> getDaXacMinh() {
        return goc.getDaXacMinh();
    }

    public LinkedHashMap<String, Integer> demTheoTrangThai(LocalDate tu, LocalDate den, List<Integer> ds, Integer l) {
        return goc.demTheoTrangThai(tu, den, ds, l);
    }

    public LinkedHashMap<String, Integer> demTheoTuan(LocalDate tu, LocalDate den, List<Integer> ds, Integer l) {
        return goc.demTheoTuan(tu, den, ds, l);
    }
}

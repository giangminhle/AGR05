package vn.agr05.dal;

import java.util.List;
import vn.agr05.entity.KhuVuc;
import vn.agr05.entity.NguoiDung;

public interface IKhuVucDal {

    List<KhuVuc> getAll();

    KhuVuc getById(int maKhuVuc);

    int insert(KhuVuc kv);

    boolean update(KhuVuc kv);

    boolean coPhanCong(int maChuyenGia, int maKhuVuc);

    void insertPhanCong(int maChuyenGia, int maKhuVuc);

    boolean deletePhanCong(int maChuyenGia, int maKhuVuc);

    /** Mọi chuyên gia được giao khu vực (kể cả đã khóa), để hiện trên KhuVucPage. */
    List<NguoiDung> getChuyenGia(int maKhuVuc);
}

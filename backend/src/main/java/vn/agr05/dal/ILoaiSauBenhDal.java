package vn.agr05.dal;

import java.util.List;
import vn.agr05.entity.LoaiSauBenh;

public interface ILoaiSauBenhDal {

    List<LoaiSauBenh> getAll();

    LoaiSauBenh getById(int maLoai);

    int insert(LoaiSauBenh loai);

    boolean update(LoaiSauBenh loai);

    /** Loại đã được dùng trong báo cáo hoặc cảnh báo thì không xóa được (UC13-A1). */
    boolean dangDuocDung(int maLoai);

    boolean delete(int maLoai);
}

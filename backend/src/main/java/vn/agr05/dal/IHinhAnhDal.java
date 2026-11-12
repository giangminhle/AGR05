package vn.agr05.dal;

import java.util.List;
import vn.agr05.entity.HinhAnh;

public interface IHinhAnhDal {

    List<HinhAnh> getByBaoCao(int maBaoCao);

    HinhAnh getById(int maHinhAnh);

    int insert(HinhAnh ha);

    /** Xóa một ảnh khi nông dân bỏ ảnh lúc sửa, gửi lại (UC03). Không xóa báo cáo. */
    boolean delete(int maHinhAnh);
}

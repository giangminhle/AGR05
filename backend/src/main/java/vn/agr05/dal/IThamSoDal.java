package vn.agr05.dal;

import java.util.List;
import vn.agr05.entity.ThamSoHeThong;

public interface IThamSoDal {

    List<ThamSoHeThong> getAll();

    /** null nếu không có tham số. */
    Integer getGiaTri(String maThamSo);

    boolean update(String maThamSo, int giaTri);
}

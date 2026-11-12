package vn.agr05.dal;

import java.util.List;
import vn.agr05.entity.LichSuXacMinh;

public interface ILichSuDal {

    List<LichSuXacMinh> getByBaoCao(int maBaoCao);

    int insert(LichSuXacMinh ls);
}

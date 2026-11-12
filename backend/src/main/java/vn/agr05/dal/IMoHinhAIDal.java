package vn.agr05.dal;

import java.util.List;
import vn.agr05.entity.MoHinhAI;

public interface IMoHinhAIDal {

    List<MoHinhAI> getAll();

    MoHinhAI getById(int maMoHinh);

    /** null nếu chưa có phiên bản đang dùng. */
    MoHinhAI getDangSuDung();

    int insert(MoHinhAI mh);

    /** Bỏ cờ bản cũ rồi bật bản maMoHinh. Service gọi trong một transaction. */
    void datDangSuDung(int maMoHinh);
}

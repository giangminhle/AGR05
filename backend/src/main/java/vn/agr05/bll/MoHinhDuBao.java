package vn.agr05.bll;

import java.io.Serializable;
import java.util.List;

/** Mô hình đã huấn luyện: biết cách mã hóa đặc trưng và tính xác suất bùng phát. */
public interface MoHinhDuBao extends Serializable {

    double xacSuat(DacTrung x);

    /** Mã loại, mã khu vực dùng khi mã hóa one-hot (lưu cùng mô hình). */
    List<Integer> dsLoai();

    List<Integer> dsKhuVuc();
}

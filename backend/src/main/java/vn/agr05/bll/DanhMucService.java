package vn.agr05.bll;

import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import vn.agr05.dal.IKhuVucDal;
import vn.agr05.dal.ILoaiSauBenhDal;
import vn.agr05.dal.INguoiDungDal;
import vn.agr05.entity.KhuVuc;
import vn.agr05.entity.LoaiSauBenh;
import vn.agr05.entity.NghiepVuException;
import vn.agr05.entity.NguoiDung;
import vn.agr05.entity.VaiTro;

/** Danh mục sâu bệnh, khu vực, phân công chuyên gia (UC13, UC14). */
@Service
public class DanhMucService {

    private final ILoaiSauBenhDal loaiSauBenhDal;
    private final IKhuVucDal khuVucDal;
    private final INguoiDungDal nguoiDungDal;

    public DanhMucService(ILoaiSauBenhDal loaiSauBenhDal, IKhuVucDal khuVucDal, INguoiDungDal nguoiDungDal) {
        this.loaiSauBenhDal = loaiSauBenhDal;
        this.khuVucDal = khuVucDal;
        this.nguoiDungDal = nguoiDungDal;
    }

    public List<LoaiSauBenh> layLoaiSauBenh() {
        return loaiSauBenhDal.getAll();
    }

    /** Thêm (maLoai = null) hoặc sửa. Trả về mã loại. */
    public int luuLoaiSauBenh(LoaiSauBenh loai) {
        if (loai.getTenLoai() == null || loai.getTenLoai().isBlank()) {
            throw NghiepVuException.khongHopLe("Vui lòng nhập tên loại sâu bệnh.", "tenLoai");
        }
        loai.setTenLoai(loai.getTenLoai().trim());
        try {
            if (loai.getMaLoai() == null) {
                return loaiSauBenhDal.insert(loai);
            }
            if (!loaiSauBenhDal.update(loai)) {
                throw NghiepVuException.khongTimThay("Không tìm thấy loại sâu bệnh.");
            }
            return loai.getMaLoai();
        } catch (DuplicateKeyException e) {
            throw NghiepVuException.khongHopLe("Tên loại sâu bệnh đã tồn tại.", "tenLoai");
        }
    }

    /** UC13-A1: loại đã được dùng trong báo cáo hoặc cảnh báo thì không cho xóa. */
    public boolean xoaLoaiSauBenh(int maLoai) {
        if (loaiSauBenhDal.dangDuocDung(maLoai)) {
            throw NghiepVuException.xungDot("Loại sâu bệnh đang được dùng trong báo cáo hoặc cảnh báo, không xóa được.");
        }
        return loaiSauBenhDal.delete(maLoai);
    }

    public List<KhuVuc> layKhuVuc() {
        return khuVucDal.getAll();
    }

    /** Thêm (maKhuVuc = null) hoặc sửa. Trả về mã khu vực. */
    public int luuKhuVuc(KhuVuc kv) {
        if (kv.getTenKhuVuc() == null || kv.getTenKhuVuc().isBlank()) {
            throw NghiepVuException.khongHopLe("Vui lòng nhập tên khu vực.", "tenKhuVuc");
        }
        kv.setTenKhuVuc(kv.getTenKhuVuc().trim());
        try {
            if (kv.getMaKhuVuc() == null) {
                return khuVucDal.insert(kv);
            }
            if (!khuVucDal.update(kv)) {
                throw NghiepVuException.khongTimThay("Không tìm thấy khu vực.");
            }
            return kv.getMaKhuVuc();
        } catch (DuplicateKeyException e) {
            throw NghiepVuException.khongHopLe("Tên khu vực đã tồn tại.", "tenKhuVuc");
        }
    }

    public List<NguoiDung> layChuyenGiaCuaKhuVuc(int maKV) {
        return khuVucDal.getChuyenGia(maKV);
    }

    /** UC14: chỉ giao chuyên gia chưa bị khóa; trùng thì báo (UC14-A4). */
    public boolean phanCong(int maCG, int maKV) {
        NguoiDung cg = nguoiDungDal.getById(maCG);
        if (cg == null || !VaiTro.CHUYEN_GIA.equals(cg.getVaiTro()) || cg.isBiKhoa()) {
            throw NghiepVuException.khongHopLe("Chỉ phân công được chuyên gia chưa bị khóa.", "chonChuyenGia");
        }
        if (khuVucDal.getById(maKV) == null) {
            throw NghiepVuException.khongTimThay("Không tìm thấy khu vực.");
        }
        if (khuVucDal.coPhanCong(maCG, maKV)) {
            throw NghiepVuException.khongHopLe("Chuyên gia đã được giao khu vực này.", "chonChuyenGia");
        }
        khuVucDal.insertPhanCong(maCG, maKV);
        return true;
    }

    public boolean goPhanCong(int maCG, int maKV) {
        return khuVucDal.deletePhanCong(maCG, maKV);
    }

    /** UC14-A3: khu vực không còn chuyên gia thì giao diện nhắc phân công người khác. */
    public boolean conChuyenGia(int maKV) {
        KhuVuc kv = khuVucDal.getById(maKV);
        return kv != null && kv.getSoChuyenGia() > 0;
    }
}

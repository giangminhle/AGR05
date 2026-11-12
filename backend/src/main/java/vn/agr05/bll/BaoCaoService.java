package vn.agr05.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import vn.agr05.dal.IBaoCaoDal;
import vn.agr05.dal.IHinhAnhDal;
import vn.agr05.dal.IKhuVucDal;
import vn.agr05.dal.ILichSuDal;
import vn.agr05.dal.INguoiDungDal;
import vn.agr05.entity.BaoCaoHienTruong;
import vn.agr05.entity.HinhAnh;
import vn.agr05.entity.LichSuXacMinh;
import vn.agr05.entity.LoaiThongBao;
import vn.agr05.entity.NghiepVuException;
import vn.agr05.entity.NguoiDung;
import vn.agr05.entity.ThamSo;
import vn.agr05.entity.TrangThai;
import vn.agr05.entity.VaiTro;

/** Gửi báo cáo, sửa và gửi lại, lấy báo cáo của nông dân (UC02, UC03, UC04). */
@Service
public class BaoCaoService {

    private final IBaoCaoDal baoCaoDal;
    private final IHinhAnhDal hinhAnhDal;
    private final ILichSuDal lichSuDal;
    private final INguoiDungDal nguoiDungDal;
    private final IKhuVucDal khuVucDal;
    private final ThongBaoService thongBaoService;
    private final ThamSoService thamSoService;
    private final AnhStorage anhStorage;
    private final TransactionTemplate tx;

    public BaoCaoService(IBaoCaoDal baoCaoDal, IHinhAnhDal hinhAnhDal, ILichSuDal lichSuDal, INguoiDungDal nguoiDungDal,
                         IKhuVucDal khuVucDal, ThongBaoService thongBaoService, ThamSoService thamSoService,
                         AnhStorage anhStorage, TransactionTemplate tx) {
        this.baoCaoDal = baoCaoDal;
        this.hinhAnhDal = hinhAnhDal;
        this.lichSuDal = lichSuDal;
        this.nguoiDungDal = nguoiDungDal;
        this.khuVucDal = khuVucDal;
        this.thongBaoService = thongBaoService;
        this.thamSoService = thamSoService;
        this.anhStorage = anhStorage;
        this.tx = tx;
    }

    /**
     * Mục 3.3.2: kiểm tra BR01 → kiểm tra file → lưu file → một transaction (báo cáo, ảnh, lịch sử, thông báo).
     * Lỗi thì Rollback và xóa file đã lưu (TC04). bc.maNguoiGui do Controller lấy từ AppSession.
     */
    public int guiBaoCao(BaoCaoHienTruong bc, List<FileTai> dsFileAnh) {
        List<FileTai> dsAnh = dsFileAnh == null ? List.of() : dsFileAnh;
        kiemTraNoiDung(bc, dsAnh.size(), dsAnh);
        bc.setTrangThai(null);
        bc.chuyenTrangThai(TrangThai.CHO_XAC_MINH);
        bc.setSoLanGuiLai(0);
        bc.setQuaHan(false);

        List<String> dsDaLuu = new ArrayList<>();
        try {
            for (FileTai f : dsAnh) {
                dsDaLuu.add(anhStorage.luu(f));
            }
            Integer ma = tx.execute(s -> {
                int maBC = baoCaoDal.insert(bc);
                for (String duongDan : dsDaLuu) {
                    HinhAnh ha = new HinhAnh();
                    ha.setMaBaoCao(maBC);
                    ha.setDuongDan(duongDan);
                    hinhAnhDal.insert(ha);
                }
                lichSuDal.insert(lichSu(maBC, bc.getMaNguoiGui(), null, TrangThai.CHO_XAC_MINH, "Gửi báo cáo"));
                thongBaoService.taoThongBaoChoNhieu(nguoiDungDal.getChuyenGiaTheoKhuVuc(bc.getMaKhuVuc()),
                        LoaiThongBao.BAO_CAO_MOI, maBC, "Báo cáo mới: " + bc.getTieuDe(),
                        "Có báo cáo mới \"" + bc.getTieuDe() + "\" cần xác minh trong khu vực bạn phụ trách.");
                return maBC;
            });
            bc.setMaBaoCao(ma);
            return Objects.requireNonNull(ma);
        } catch (RuntimeException e) {
            dsDaLuu.forEach(anhStorage::xoa);
            throw e;
        }
    }

    /** UC04: báo cáo của người đang đăng nhập, kèm ảnh, lịch sử và cờ được sửa. */
    public List<BaoCaoHienTruong> layCuaToi(int maNguoiDung) {
        int hanNgay = thamSoService.layGiaTri(ThamSo.HAN_GUI_LAI_NGAY);
        List<BaoCaoHienTruong> ds = baoCaoDal.getByNguoiGui(maNguoiDung);
        for (BaoCaoHienTruong bc : ds) {
            napChiTiet(bc);
            bc.setDuocSua(bc.choPhepSua(maNguoiDung, hanNgay));
        }
        return ds;
    }

    /** Mục 3.3.8: lấy báo cáo để sửa, entity quyết định có cho sửa không (choPhepSua). */
    public BaoCaoHienTruong layDeSua(int ma, int maNguoiDung) {
        BaoCaoHienTruong bc = baoCaoDal.getById(ma);
        if (bc == null) {
            throw NghiepVuException.khongTimThay("Không tìm thấy báo cáo.");
        }
        if (bc.getMaNguoiGui() != maNguoiDung) {
            throw NghiepVuException.khongDuQuyen("Bạn chỉ được sửa báo cáo của mình.");
        }
        int hanNgay = thamSoService.layGiaTri(ThamSo.HAN_GUI_LAI_NGAY);
        if (!bc.choPhepSua(maNguoiDung, hanNgay)) {
            if (TrangThai.BI_BAC_BO.equals(bc.getTrangThai()) && bc.getSoLanGuiLai() >= 1) {
                throw NghiepVuException.xungDot("Báo cáo đã hết lượt gửi lại (chỉ được gửi lại 1 lần).");
            }
            if (TrangThai.BI_BAC_BO.equals(bc.getTrangThai())) {
                throw NghiepVuException.xungDot("Đã quá " + hanNgay + " ngày, không thể gửi lại báo cáo.");
            }
            throw NghiepVuException.xungDot("Báo cáo đang ở trạng thái " + TrangThai.ten(bc.getTrangThai())
                    + ", không được sửa.");
        }
        napChiTiet(bc);
        bc.setDuocSua(true);
        return bc;
    }

    /**
     * Sửa (khi còn Chờ xác minh) hoặc gửi lại (khi Bị bác bỏ). bc mang nội dung mới, maBaoCao và maNguoiGui (người đang đăng nhập).
     * dsXoa là mã các ảnh cũ muốn bỏ.
     */
    public boolean guiLai(BaoCaoHienTruong bc, List<FileTai> dsMoi, List<Integer> dsXoa) {
        List<FileTai> anhMoi = dsMoi == null ? List.of() : dsMoi;
        BaoCaoHienTruong cu = layDeSua(bc.getMaBaoCao(), bc.getMaNguoiGui());
        List<HinhAnh> anhBo = cu.getDsHinhAnh().stream()
                .filter(h -> dsXoa != null && dsXoa.contains(h.getMaHinhAnh())).toList();
        int soAnh = cu.getDsHinhAnh().size() - anhBo.size() + anhMoi.size();

        cu.setTieuDe(bc.getTieuDe());
        cu.setTenCayTrong(bc.getTenCayTrong());
        cu.setMoTa(bc.getMoTa());
        cu.setMucDo(bc.getMucDo());
        cu.setViDo(bc.getViDo());
        cu.setKinhDo(bc.getKinhDo());
        cu.setNgayPhatHien(bc.getNgayPhatHien());
        cu.setMaKhuVuc(bc.getMaKhuVuc());
        kiemTraNoiDung(cu, soAnh, anhMoi);

        String trangThaiCu = cu.getTrangThai();
        boolean laGuiLai = TrangThai.BI_BAC_BO.equals(trangThaiCu);
        if (laGuiLai) {
            cu.chuyenTrangThai(TrangThai.CHO_XAC_MINH);
            cu.setSoLanGuiLai(cu.getSoLanGuiLai() + 1);
            cu.setQuaHan(false);
        }

        List<String> dsDaLuu = new ArrayList<>();
        try {
            for (FileTai f : anhMoi) {
                dsDaLuu.add(anhStorage.luu(f));
            }
            tx.executeWithoutResult(s -> {
                if (!baoCaoDal.updateNoiDung(cu, trangThaiCu)) {
                    throw NghiepVuException.xungDot("Báo cáo đã được xử lý, không thể sửa.");
                }
                for (HinhAnh h : anhBo) {
                    hinhAnhDal.delete(h.getMaHinhAnh());
                }
                for (String duongDan : dsDaLuu) {
                    HinhAnh ha = new HinhAnh();
                    ha.setMaBaoCao(cu.getMaBaoCao());
                    ha.setDuongDan(duongDan);
                    hinhAnhDal.insert(ha);
                }
                lichSuDal.insert(lichSu(cu.getMaBaoCao(), cu.getMaNguoiGui(), trangThaiCu, TrangThai.CHO_XAC_MINH,
                        laGuiLai ? "Gửi lại lần " + cu.getSoLanGuiLai() : "Sửa nội dung báo cáo"));
                thongBaoService.taoThongBaoChoNhieu(nguoiDungDal.getChuyenGiaTheoKhuVuc(cu.getMaKhuVuc()),
                        LoaiThongBao.BAO_CAO_MOI, cu.getMaBaoCao(),
                        (laGuiLai ? "Báo cáo gửi lại: " : "Báo cáo đã sửa: ") + cu.getTieuDe(),
                        "Báo cáo \"" + cu.getTieuDe() + "\" vừa được " + (laGuiLai ? "gửi lại" : "sửa") + ", cần xác minh.");
            });
        } catch (RuntimeException e) {
            dsDaLuu.forEach(anhStorage::xoa);
            throw e;
        }
        // Chỉ xóa file ảnh cũ sau khi đã Commit
        anhBo.forEach(h -> anhStorage.xoa(h.getDuongDan()));
        return true;
    }

    /**
     * Trả ảnh cho giao diện có kiểm tra quyền xem: chủ báo cáo, chuyên gia khu vực, Quản trị viên.
     */
    public AnhTai layAnh(int maHinhAnh, NguoiDung nguoiXem, List<Integer> dsKhuVucPhuTrach) {
        HinhAnh ha = hinhAnhDal.getById(maHinhAnh);
        BaoCaoHienTruong bc = ha == null ? null : baoCaoDal.getById(ha.getMaBaoCao());
        if (bc == null) {
            throw NghiepVuException.khongTimThay("Không tìm thấy ảnh.");
        }
        boolean duocXem = switch (nguoiXem.getVaiTro()) {
            case VaiTro.QUAN_TRI -> true;
            case VaiTro.CHUYEN_GIA -> dsKhuVucPhuTrach.contains(bc.getMaKhuVuc());
            default -> Objects.equals(bc.getMaNguoiGui(), nguoiXem.getMaNguoiDung());
        };
        if (!duocXem) {
            throw NghiepVuException.khongDuQuyen("Bạn không có quyền xem ảnh này.");
        }
        return anhStorage.doc(ha.getDuongDan());
    }

    private void kiemTraNoiDung(BaoCaoHienTruong bc, int soAnh, List<FileTai> anhMoi) {
        String o = bc.kiemTraHopLe(soAnh);
        if (o != null) {
            throw NghiepVuException.khongHopLe(BaoCaoHienTruong.loiHopLe(o), o.startsWith("anh") ? "anh" : o);
        }
        if (khuVucDal.getById(bc.getMaKhuVuc()) == null) {
            throw NghiepVuException.khongHopLe("Khu vực không tồn tại.", "maKhuVuc");
        }
        for (FileTai f : anhMoi) {
            String loi = anhStorage.lyDoLoi(f);
            if (loi != null) {
                throw NghiepVuException.khongHopLe(loi, "anh");
            }
        }
    }

    private void napChiTiet(BaoCaoHienTruong bc) {
        bc.setDsHinhAnh(hinhAnhDal.getByBaoCao(bc.getMaBaoCao()));
        bc.setDsLichSu(lichSuDal.getByBaoCao(bc.getMaBaoCao()));
    }

    static LichSuXacMinh lichSu(int maBC, Integer nguoi, String cu, String moi, String ghiChu) {
        LichSuXacMinh ls = new LichSuXacMinh();
        ls.setMaBaoCao(maBC);
        ls.setMaNguoiThucHien(nguoi);
        ls.setTrangThaiCu(cu);
        ls.setTrangThaiMoi(moi);
        ls.setGhiChu(ghiChu);
        return ls;
    }
}

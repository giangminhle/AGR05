package vn.agr05.bll;

import java.nio.file.Path;
import java.util.List;
import vn.agr05.entity.DuBaoNguyCo;
import vn.agr05.fake.FakeDb;
import vn.agr05.fake.FakeTxManager;

/** Lắp các Service thật với DAL giả (cùng gói bll để dùng được constructor test của AIService). */
class HeThongGia {

    final FakeDb db = new FakeDb();
    final FakeTxManager txm = new FakeTxManager();
    final AppSession session = new AppSession();
    final ThamSoService thamSo = new ThamSoService(db.thamSoDal);
    final ThongBaoService thongBao = new ThongBaoService(db.thongBaoDal);
    final AnhStorage anh;
    final CanhBaoService canhBao = new CanhBaoService(db.canhBaoDal, db.baoCaoDal, db.nguoiDungDal, db.khuVucDal,
            db.loaiDal, thongBao, thamSo);
    final HocMayGia hocMay = new HocMayGia();
    final AIService ai;
    final BaoCaoService baoCao;
    final XacMinhService xacMinh;
    final AccountService account = new AccountService(db.nguoiDungDal, session);
    final HanXuLyService hanXuLy;

    HeThongGia(Path thuMucTam) {
        anh = new AnhStorage(thuMucTam.resolve("anh"));
        ai = new AIService(db.moHinhDal, db.duBaoDal, db.baoCaoDal, thamSo, canhBao, txm.template(), hocMay,
                thuMucTam.resolve("mo-hinh"));
        baoCao = new BaoCaoService(db.baoCaoDal, db.hinhAnhDal, db.lichSuDal, db.nguoiDungDal, db.khuVucDal, thongBao,
                thamSo, anh, txm.template());
        xacMinh = new XacMinhService(db.baoCaoDal, db.hinhAnhDal, db.lichSuDal, db.nguoiDungDal, db.loaiDal, thongBao,
                canhBao, thamSo, ai, txm.template());
        hanXuLy = new HanXuLyService(db.baoCaoDal, db.lichSuDal, db.nguoiDungDal, thongBao, thamSo, txm.template());
    }

    /** Thư viện học máy giả: xác suất cố định, lưu và nạp trong bộ nhớ. */
    static class HocMayGia implements AIService.ThuVienHocMay {

        double xacSuat = 0.1;
        boolean loiKhiNap;

        @Override
        public MoHinhDuBao huanLuyen(List<DacTrung> x, int[] y, List<Integer> dsLoai, List<Integer> dsKhuVuc) {
            return moHinh();
        }

        @Override
        public void luu(MoHinhDuBao moHinh, Path file) {
            // không ghi file
        }

        @Override
        public MoHinhDuBao nap(Path file) {
            if (loiKhiNap) {
                throw new IllegalStateException("File mô hình hỏng (giả lập)");
            }
            return moHinh();
        }

        MoHinhDuBao moHinh() {
            return new MoHinhDuBao() {
                @Override
                public double xacSuat(DacTrung d) {
                    return xacSuat;
                }

                @Override
                public List<Integer> dsLoai() {
                    return List.of();
                }

                @Override
                public List<Integer> dsKhuVuc() {
                    return List.of();
                }
            };
        }
    }

    static DuBaoNguyCo khongDung() {
        return null;
    }
}

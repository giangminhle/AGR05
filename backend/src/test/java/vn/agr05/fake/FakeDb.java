package vn.agr05.fake;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import vn.agr05.dal.IBaoCaoDal;
import vn.agr05.dal.ICanhBaoDal;
import vn.agr05.dal.IDuBaoDal;
import vn.agr05.dal.IHinhAnhDal;
import vn.agr05.dal.IKhuVucDal;
import vn.agr05.dal.ILichSuDal;
import vn.agr05.dal.ILoaiSauBenhDal;
import vn.agr05.dal.IMoHinhAIDal;
import vn.agr05.dal.INguoiDungDal;
import vn.agr05.dal.IThamSoDal;
import vn.agr05.dal.IThongBaoDal;
import vn.agr05.entity.BaoCaoHienTruong;
import vn.agr05.entity.CanhBao;
import vn.agr05.entity.DuBaoNguyCo;
import vn.agr05.entity.HinhAnh;
import vn.agr05.entity.KhuVuc;
import vn.agr05.entity.LichSuXacMinh;
import vn.agr05.entity.LoaiSauBenh;
import vn.agr05.entity.MatKhau;
import vn.agr05.entity.MoHinhAI;
import vn.agr05.entity.NguoiDung;
import vn.agr05.entity.ThamSo;
import vn.agr05.entity.ThamSoHeThong;
import vn.agr05.entity.ThongBao;
import vn.agr05.entity.TrangThai;
import vn.agr05.entity.VaiTro;

/** CSDL giả trong bộ nhớ, cài đủ các interface I...Dal cho test BLL (không cần SQL Server). */
public class FakeDb {

    public final List<NguoiDung> nguoiDung = new ArrayList<>();
    public final List<KhuVuc> khuVuc = new ArrayList<>();
    public final List<int[]> phanCong = new ArrayList<>();
    public final List<LoaiSauBenh> loai = new ArrayList<>();
    public final List<BaoCaoHienTruong> baoCao = new ArrayList<>();
    public final List<HinhAnh> hinhAnh = new ArrayList<>();
    public final List<LichSuXacMinh> lichSu = new ArrayList<>();
    public final List<ThongBao> thongBao = new ArrayList<>();
    public final List<CanhBao> canhBao = new ArrayList<>();
    public final List<DuBaoNguyCo> duBao = new ArrayList<>();
    public final List<MoHinhAI> moHinh = new ArrayList<>();
    public final Map<String, Integer> thamSo = new HashMap<>();
    private int ma = 100;

    /** Bật để giả lập mất kết nối CSDL khi ghi ảnh (TC04). */
    public boolean loiKhiGhiAnh;

    public FakeDb() {
        thamSo.put(ThamSo.HAN_XAC_MINH_GIO, 48);
        thamSo.put(ThamSo.NGUONG_CANH_BAO, 5);
        thamSo.put(ThamSo.SO_NGAY_XET_NGUONG, 7);
        thamSo.put(ThamSo.HAN_GUI_LAI_NGAY, 7);
        thamSo.put(ThamSo.NGUONG_NGUY_CO, 70);
    }

    private int maMoi() {
        return ++ma;
    }

    // ------------------------------------------------------------ dữ liệu mẫu
    public NguoiDung themNguoiDung(String ten, String vaiTro, String matKhau) {
        NguoiDung nd = new NguoiDung();
        nd.setMaNguoiDung(maMoi());
        nd.setTenDangNhap(ten);
        nd.setHoTen(ten);
        nd.setVaiTro(vaiTro);
        nd.setMuoi(MatKhau.taoMuoi());
        nd.setMatKhauHash(MatKhau.bam(matKhau, nd.getMuoi()));
        nguoiDung.add(nd);
        return nd;
    }

    public KhuVuc themKhuVuc(String ten) {
        KhuVuc kv = new KhuVuc();
        kv.setMaKhuVuc(maMoi());
        kv.setTenKhuVuc(ten);
        khuVuc.add(kv);
        return kv;
    }

    public LoaiSauBenh themLoai(String ten) {
        LoaiSauBenh l = new LoaiSauBenh();
        l.setMaLoai(maMoi());
        l.setTenLoai(ten);
        loai.add(l);
        return l;
    }

    public void phanCong(int maCG, int maKV) {
        phanCong.add(new int[]{maCG, maKV});
    }

    /** Báo cáo đã có sẵn trong CSDL ở trạng thái tt. */
    public BaoCaoHienTruong themBaoCao(int maNguoiGui, int maKV, String tt) {
        BaoCaoHienTruong bc = mauBaoCao(maKV);
        bc.setMaBaoCao(maMoi());
        bc.setMaNguoiGui(maNguoiGui);
        bc.setTrangThai(tt);
        bc.setNgayGui(LocalDateTime.now());
        bc.setNgayVaoTrangThai(LocalDateTime.now());
        baoCao.add(bc);
        HinhAnh ha = new HinhAnh();
        ha.setMaHinhAnh(maMoi());
        ha.setMaBaoCao(bc.getMaBaoCao());
        ha.setDuongDan("cu.jpg");
        hinhAnh.add(ha);
        return bc;
    }

    /** Một ca đã xác minh lúc thoiGian (dùng cho BR06, AI). */
    public BaoCaoHienTruong themCaDaXacMinh(int maNguoiGui, int maKV, int maLoai, LocalDateTime thoiGian) {
        BaoCaoHienTruong bc = themBaoCao(maNguoiGui, maKV, TrangThai.DA_XAC_MINH);
        bc.setMaLoai(maLoai);
        LichSuXacMinh ls = new LichSuXacMinh();
        ls.setMaLichSu(maMoi());
        ls.setMaBaoCao(bc.getMaBaoCao());
        ls.setTrangThaiCu(TrangThai.CHO_XAC_MINH);
        ls.setTrangThaiMoi(TrangThai.DA_XAC_MINH);
        ls.setThoiGian(thoiGian);
        lichSu.add(ls);
        return bc;
    }

    public static BaoCaoHienTruong mauBaoCao(int maKV) {
        BaoCaoHienTruong bc = new BaoCaoHienTruong();
        bc.setTieuDe("Lúa vàng lá");
        bc.setTenCayTrong("Lúa");
        bc.setMoTa("Nhiều cây bị vàng lá thành chòm");
        bc.setMucDo(2);
        bc.setViDo(new BigDecimal("10.77"));
        bc.setKinhDo(new BigDecimal("106.70"));
        bc.setNgayPhatHien(LocalDate.now());
        bc.setMaKhuVuc(maKV);
        return bc;
    }

    public List<ThongBao> thongBaoCua(int maNguoiNhan) {
        return thongBao.stream().filter(t -> t.getMaNguoiNhan() == maNguoiNhan).toList();
    }

    public BaoCaoHienTruong timBaoCao(int ma) {
        return baoCao.stream().filter(b -> b.getMaBaoCao() == ma).findFirst().orElse(null);
    }

    private static BaoCaoHienTruong sao(BaoCaoHienTruong b) {
        BaoCaoHienTruong c = new BaoCaoHienTruong();
        c.setMaBaoCao(b.getMaBaoCao());
        c.setTieuDe(b.getTieuDe());
        c.setTenCayTrong(b.getTenCayTrong());
        c.setMoTa(b.getMoTa());
        c.setMucDo(b.getMucDo());
        c.setViDo(b.getViDo());
        c.setKinhDo(b.getKinhDo());
        c.setTrangThai(b.getTrangThai());
        c.setKhuyenNghi(b.getKhuyenNghi());
        c.setNgayPhatHien(b.getNgayPhatHien());
        c.setNgayGui(b.getNgayGui());
        c.setNgayVaoTrangThai(b.getNgayVaoTrangThai());
        c.setSoLanGuiLai(b.getSoLanGuiLai());
        c.setQuaHan(b.isQuaHan());
        c.setMaNguoiGui(b.getMaNguoiGui());
        c.setMaKhuVuc(b.getMaKhuVuc());
        c.setMaLoai(b.getMaLoai());
        c.setNgayXacMinh(b.getNgayXacMinh());
        return c;
    }

    private LocalDateTime ngayXacMinh(int maBC) {
        return lichSu.stream().filter(l -> l.getMaBaoCao() == maBC && TrangThai.DA_XAC_MINH.equals(l.getTrangThaiMoi()))
                .map(LichSuXacMinh::getThoiGian).filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null);
    }

    private List<BaoCaoHienTruong> caDaXacMinh(int kv, int loaiMa, LocalDateTime tu, LocalDateTime den) {
        return baoCao.stream().filter(b -> b.getMaKhuVuc() == kv && Objects.equals(b.getMaLoai(), loaiMa)
                && (TrangThai.DA_XAC_MINH.equals(b.getTrangThai()) || TrangThai.DA_XU_LY.equals(b.getTrangThai())))
                .filter(b -> {
                    LocalDateTime t = ngayXacMinh(b.getMaBaoCao());
                    return t != null && !t.isBefore(tu) && t.isBefore(den);
                }).toList();
    }

    // ------------------------------------------------------------ các DAL giả
    public final INguoiDungDal nguoiDungDal = new INguoiDungDal() {
        public NguoiDung getByTenDangNhap(String t) {
            return nguoiDung.stream().filter(n -> n.getTenDangNhap().equals(t)).findFirst().orElse(null);
        }

        public NguoiDung getById(int m) {
            return nguoiDung.stream().filter(n -> n.getMaNguoiDung() == m).findFirst().orElse(null);
        }

        public List<NguoiDung> getAll() {
            return List.copyOf(nguoiDung);
        }

        public int insert(NguoiDung nd) {
            nd.setMaNguoiDung(maMoi());
            nguoiDung.add(nd);
            return nd.getMaNguoiDung();
        }

        public boolean update(NguoiDung nd) {
            NguoiDung cu = getById(nd.getMaNguoiDung());
            cu.setHoTen(nd.getHoTen());
            cu.setVaiTro(nd.getVaiTro());
            return true;
        }

        public boolean updateBiKhoa(int m, boolean k) {
            getById(m).setBiKhoa(k);
            return true;
        }

        public List<Integer> getKhuVucPhuTrach(int m) {
            return phanCong.stream().filter(p -> p[0] == m).map(p -> p[1]).toList();
        }

        public List<NguoiDung> getChuyenGiaTheoKhuVuc(int kv) {
            return nguoiDung.stream().filter(n -> !n.isBiKhoa() && VaiTro.CHUYEN_GIA.equals(n.getVaiTro())
                    && phanCong.stream().anyMatch(p -> p[0] == n.getMaNguoiDung() && p[1] == kv)).toList();
        }

        public List<NguoiDung> getNongDanTheoKhuVuc(int kv) {
            return nguoiDung.stream().filter(n -> !n.isBiKhoa() && VaiTro.NONG_DAN.equals(n.getVaiTro())
                    && baoCao.stream().anyMatch(b -> b.getMaKhuVuc() == kv && b.getMaNguoiGui() == n.getMaNguoiDung())).toList();
        }

        public List<NguoiDung> getQuanTri() {
            return nguoiDung.stream().filter(n -> !n.isBiKhoa() && VaiTro.QUAN_TRI.equals(n.getVaiTro())).toList();
        }
    };

    public final IKhuVucDal khuVucDal = new IKhuVucDal() {
        public List<KhuVuc> getAll() {
            return List.copyOf(khuVuc);
        }

        public KhuVuc getById(int m) {
            KhuVuc kv = khuVuc.stream().filter(k -> k.getMaKhuVuc() == m).findFirst().orElse(null);
            if (kv != null) {
                kv.setSoChuyenGia((int) phanCong.stream().filter(p -> p[1] == m).count());
            }
            return kv;
        }

        public int insert(KhuVuc kv) {
            kv.setMaKhuVuc(maMoi());
            khuVuc.add(kv);
            return kv.getMaKhuVuc();
        }

        public boolean update(KhuVuc kv) {
            return true;
        }

        public boolean coPhanCong(int cg, int kv) {
            return phanCong.stream().anyMatch(p -> p[0] == cg && p[1] == kv);
        }

        public void insertPhanCong(int cg, int kv) {
            phanCong(cg, kv);
        }

        public boolean deletePhanCong(int cg, int kv) {
            return phanCong.removeIf(p -> p[0] == cg && p[1] == kv);
        }

        public List<NguoiDung> getChuyenGia(int kv) {
            return nguoiDung.stream().filter(n -> coPhanCong(n.getMaNguoiDung(), kv)).toList();
        }
    };

    public final ILoaiSauBenhDal loaiDal = new ILoaiSauBenhDal() {
        public List<LoaiSauBenh> getAll() {
            return List.copyOf(loai);
        }

        public LoaiSauBenh getById(int m) {
            return loai.stream().filter(l -> l.getMaLoai() == m).findFirst().orElse(null);
        }

        public int insert(LoaiSauBenh l) {
            l.setMaLoai(maMoi());
            loai.add(l);
            return l.getMaLoai();
        }

        public boolean update(LoaiSauBenh l) {
            return true;
        }

        public boolean dangDuocDung(int m) {
            return baoCao.stream().anyMatch(b -> Objects.equals(b.getMaLoai(), m));
        }

        public boolean delete(int m) {
            return loai.removeIf(l -> l.getMaLoai() == m);
        }
    };

    public final IBaoCaoDal baoCaoDal = new IBaoCaoDal() {
        public int insert(BaoCaoHienTruong bc) {
            BaoCaoHienTruong c = sao(bc);
            c.setMaBaoCao(maMoi());
            c.setNgayGui(LocalDateTime.now());
            baoCao.add(c);
            return c.getMaBaoCao();
        }

        public BaoCaoHienTruong getById(int m) {
            BaoCaoHienTruong b = timBaoCao(m);
            return b == null ? null : sao(b);
        }

        public List<BaoCaoHienTruong> getByNguoiGui(int m) {
            return baoCao.stream().filter(b -> b.getMaNguoiGui() == m).map(FakeDb::sao).toList();
        }

        public List<BaoCaoHienTruong> getByTrangThai(String tt, List<Integer> ds) {
            return baoCao.stream().filter(b -> b.getTrangThai().equals(tt) && (ds == null || ds.contains(b.getMaKhuVuc())))
                    .map(FakeDb::sao).toList();
        }

        public List<BaoCaoHienTruong> getQuaHan(int hanGio) {
            return baoCao.stream().filter(b -> !b.isQuaHan() && b.laQuaHan(hanGio)).map(FakeDb::sao).toList();
        }

        public boolean danhDauQuaHan(int m) {
            timBaoCao(m).setQuaHan(true);
            return true;
        }

        public List<BaoCaoHienTruong> getBiBacBoHetHan(int hanNgay) {
            return baoCao.stream().filter(b -> b.getSoLanGuiLai() == 0 && b.hetHanGuiLai(hanNgay)).map(FakeDb::sao).toList();
        }

        public boolean updateTrangThai(BaoCaoHienTruong bc, String ttCu) {
            BaoCaoHienTruong b = timBaoCao(bc.getMaBaoCao());
            if (b == null || !b.getTrangThai().equals(ttCu)) {
                return false;
            }
            b.setTrangThai(bc.getTrangThai());
            b.setNgayVaoTrangThai(bc.getNgayVaoTrangThai());
            b.setMaLoai(bc.getMaLoai());
            b.setKhuyenNghi(bc.getKhuyenNghi());
            return true;
        }

        public boolean updateNoiDung(BaoCaoHienTruong bc, String ttCu) {
            BaoCaoHienTruong b = timBaoCao(bc.getMaBaoCao());
            if (b == null || !b.getTrangThai().equals(ttCu)) {
                return false;
            }
            baoCao.set(baoCao.indexOf(b), sao(bc));
            return true;
        }

        public int demCaDaXacMinh(int kv, int l, LocalDateTime tu, LocalDateTime den) {
            return caDaXacMinh(kv, l, tu, den).size();
        }

        public double mucDoTrungBinh(int kv, int l, LocalDateTime tu, LocalDateTime den) {
            return caDaXacMinh(kv, l, tu, den).stream().mapToInt(BaoCaoHienTruong::getMucDo).average().orElse(0);
        }

        public List<BaoCaoHienTruong> getDaXacMinh() {
            List<BaoCaoHienTruong> kq = new ArrayList<>();
            for (BaoCaoHienTruong b : baoCao) {
                LocalDateTime t = ngayXacMinh(b.getMaBaoCao());
                if (b.getMaLoai() != null && t != null) {
                    BaoCaoHienTruong c = sao(b);
                    c.setNgayXacMinh(t);
                    kq.add(c);
                }
            }
            return kq;
        }

        public LinkedHashMap<String, Integer> demTheoTrangThai(LocalDate tu, LocalDate den, List<Integer> ds, Integer l) {
            return new LinkedHashMap<>();
        }

        public LinkedHashMap<String, Integer> demTheoTuan(LocalDate tu, LocalDate den, List<Integer> ds, Integer l) {
            return new LinkedHashMap<>();
        }
    };

    public final IHinhAnhDal hinhAnhDal = new IHinhAnhDal() {
        public List<HinhAnh> getByBaoCao(int m) {
            return hinhAnh.stream().filter(h -> h.getMaBaoCao() == m).toList();
        }

        public HinhAnh getById(int m) {
            return hinhAnh.stream().filter(h -> h.getMaHinhAnh() == m).findFirst().orElse(null);
        }

        public int insert(HinhAnh ha) {
            if (loiKhiGhiAnh) {
                throw new IllegalStateException("Mất kết nối CSDL (giả lập)");
            }
            ha.setMaHinhAnh(maMoi());
            hinhAnh.add(ha);
            return ha.getMaHinhAnh();
        }

        public boolean delete(int m) {
            return hinhAnh.removeIf(h -> h.getMaHinhAnh() == m);
        }
    };

    public final ILichSuDal lichSuDal = new ILichSuDal() {
        public List<LichSuXacMinh> getByBaoCao(int m) {
            return lichSu.stream().filter(l -> l.getMaBaoCao() == m).toList();
        }

        public int insert(LichSuXacMinh ls) {
            ls.setMaLichSu(maMoi());
            if (ls.getThoiGian() == null) {
                ls.setThoiGian(LocalDateTime.now());
            }
            lichSu.add(ls);
            return ls.getMaLichSu();
        }
    };

    public final IThongBaoDal thongBaoDal = new IThongBaoDal() {
        public int insert(ThongBao tb) {
            tb.setMaThongBao(maMoi());
            tb.setNgayTao(LocalDateTime.now());
            thongBao.add(tb);
            return tb.getMaThongBao();
        }

        public int countChuaDoc(int m) {
            return (int) thongBao.stream().filter(t -> t.getMaNguoiNhan() == m && !t.isDaDoc()).count();
        }

        public List<ThongBao> getByNguoiNhan(int m) {
            return thongBaoCua(m);
        }

        public boolean markRead(int maTB, int m) {
            for (ThongBao t : thongBao) {
                if (t.getMaThongBao() == maTB && t.getMaNguoiNhan() == m) {
                    t.setDaDoc(true);
                    return true;
                }
            }
            return false;
        }
    };

    public final ICanhBaoDal canhBaoDal = new ICanhBaoDal() {
        public int insert(CanhBao cb) {
            cb.setMaCanhBao(maMoi());
            cb.setNgayTao(LocalDateTime.now());
            canhBao.add(cb);
            return cb.getMaCanhBao();
        }

        public boolean coCanhBaoTu(int kv, int l, LocalDateTime tu) {
            return canhBao.stream().anyMatch(c -> c.getMaKhuVuc() == kv && c.getMaLoai() == l && !c.getNgayTao().isBefore(tu));
        }

        public List<CanhBao> getTrongKy(LocalDate tu, LocalDate den, List<Integer> ds, Integer l) {
            return List.copyOf(canhBao);
        }
    };

    public final IThamSoDal thamSoDal = new IThamSoDal() {
        public List<ThamSoHeThong> getAll() {
            return List.of();
        }

        public Integer getGiaTri(String m) {
            return thamSo.get(m);
        }

        public boolean update(String m, int gt) {
            thamSo.put(m, gt);
            return true;
        }
    };

    public final IDuBaoDal duBaoDal = new IDuBaoDal() {
        public int insert(DuBaoNguyCo d) {
            d.setMaDuBao(maMoi());
            duBao.add(d);
            return d.getMaDuBao();
        }

        public DuBaoNguyCo getByBaoCao(int m) {
            return duBao.stream().filter(d -> d.getMaBaoCao() == m).findFirst().orElse(null);
        }

        public LinkedHashMap<String, Integer> demTheoMuc(LocalDate tu, LocalDate den, List<Integer> ds, Integer l) {
            return new LinkedHashMap<>();
        }
    };

    public final IMoHinhAIDal moHinhDal = new IMoHinhAIDal() {
        public List<MoHinhAI> getAll() {
            return List.copyOf(moHinh);
        }

        public MoHinhAI getById(int m) {
            return moHinh.stream().filter(x -> x.getMaMoHinh() == m).findFirst().orElse(null);
        }

        public MoHinhAI getDangSuDung() {
            return moHinh.stream().filter(MoHinhAI::isDangSuDung).findFirst().orElse(null);
        }

        public int insert(MoHinhAI mh) {
            mh.setMaMoHinh(maMoi());
            moHinh.add(mh);
            return mh.getMaMoHinh();
        }

        public void datDangSuDung(int m) {
            moHinh.forEach(x -> x.setDangSuDung(x.getMaMoHinh() == m));
        }
    };
}

package vn.agr05.api;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.agr05.api.dto.NguoiDungRutGon;
import vn.agr05.bll.DanhMucService;
import vn.agr05.entity.KhuVuc;
import vn.agr05.entity.LoaiSauBenh;

/** UC13 (danh mục sâu bệnh), UC14 (khu vực, phân công). */
@RestController
public class DanhMucController {

    private final DanhMucService danhMucService;

    public DanhMucController(DanhMucService danhMucService) {
        this.danhMucService = danhMucService;
    }

    // ---- Loại sâu bệnh
    @GetMapping("/api/loai-sau-benh")
    public List<LoaiSauBenh> layLoaiSauBenh() {
        return danhMucService.layLoaiSauBenh();
    }

    @PostMapping("/api/loai-sau-benh")
    public Map<String, Integer> themLoai(@RequestBody LoaiSauBenh loai) {
        loai.setMaLoai(null);
        return Map.of("maLoai", danhMucService.luuLoaiSauBenh(loai));
    }

    @PutMapping("/api/loai-sau-benh/{ma}")
    public Map<String, Integer> suaLoai(@PathVariable int ma, @RequestBody LoaiSauBenh loai) {
        loai.setMaLoai(ma);
        return Map.of("maLoai", danhMucService.luuLoaiSauBenh(loai));
    }

    @DeleteMapping("/api/loai-sau-benh/{ma}")
    public Map<String, Boolean> xoaLoai(@PathVariable int ma) {
        return Map.of("thanhCong", danhMucService.xoaLoaiSauBenh(ma));
    }

    // ---- Khu vực
    @GetMapping("/api/khu-vuc")
    public List<KhuVuc> layKhuVuc() {
        return danhMucService.layKhuVuc();
    }

    @PostMapping("/api/khu-vuc")
    public Map<String, Integer> themKhuVuc(@RequestBody KhuVuc kv) {
        kv.setMaKhuVuc(null);
        return Map.of("maKhuVuc", danhMucService.luuKhuVuc(kv));
    }

    @PutMapping("/api/khu-vuc/{ma}")
    public Map<String, Integer> suaKhuVuc(@PathVariable int ma, @RequestBody KhuVuc kv) {
        kv.setMaKhuVuc(ma);
        return Map.of("maKhuVuc", danhMucService.luuKhuVuc(kv));
    }

    @GetMapping("/api/khu-vuc/{ma}/chuyen-gia")
    public List<NguoiDungRutGon> layChuyenGia(@PathVariable int ma) {
        return danhMucService.layChuyenGiaCuaKhuVuc(ma).stream().map(NguoiDungRutGon::tu).toList();
    }

    public record YeuCauPhanCong(int maChuyenGia) {
    }

    @PostMapping("/api/khu-vuc/{ma}/phan-cong")
    public Map<String, Boolean> phanCong(@PathVariable int ma, @RequestBody YeuCauPhanCong yc) {
        return Map.of("thanhCong", danhMucService.phanCong(yc.maChuyenGia(), ma));
    }

    @DeleteMapping("/api/khu-vuc/{ma}/phan-cong/{maCG}")
    public Map<String, Boolean> goPhanCong(@PathVariable int ma, @PathVariable int maCG) {
        boolean ok = danhMucService.goPhanCong(maCG, ma);
        return Map.of("thanhCong", ok, "conChuyenGia", danhMucService.conChuyenGia(ma));
    }
}

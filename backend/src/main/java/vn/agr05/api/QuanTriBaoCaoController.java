package vn.agr05.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.agr05.bll.BaoCaoService;
import vn.agr05.entity.BaoCaoHienTruong;

/** Quản trị viên tra cứu báo cáo (chỉ đọc), để theo dõi báo cáo quá hạn và phân công lại. */
@RestController
@RequestMapping("/api/quan-tri/bao-cao")
public class QuanTriBaoCaoController {

    private final BaoCaoService baoCaoService;

    public QuanTriBaoCaoController(BaoCaoService baoCaoService) {
        this.baoCaoService = baoCaoService;
    }

    @GetMapping
    public List<BaoCaoHienTruong> timKiem(@RequestParam(required = false) String trangThai,
                                          @RequestParam(required = false) Integer maKhuVuc,
                                          @RequestParam(required = false) String tuKhoa) {
        return baoCaoService.timKiemQuanTri(trangThai, maKhuVuc, tuKhoa);
    }

    @GetMapping("/{ma}")
    public BaoCaoHienTruong layChiTiet(@PathVariable int ma) {
        return baoCaoService.layChiTietQuanTri(ma);
    }
}

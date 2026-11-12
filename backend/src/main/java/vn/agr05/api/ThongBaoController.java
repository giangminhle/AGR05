package vn.agr05.api;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.agr05.bll.AppSession;
import vn.agr05.bll.ThongBaoService;
import vn.agr05.entity.ThongBao;

/** UC12, UC17 (mục 3.3.6). */
@RestController
@RequestMapping("/api/thong-bao")
public class ThongBaoController {

    private final ThongBaoService thongBaoService;
    private final AppSession appSession;

    public ThongBaoController(ThongBaoService thongBaoService, AppSession appSession) {
        this.thongBaoService = thongBaoService;
        this.appSession = appSession;
    }

    @GetMapping
    public List<ThongBao> layThongBao() {
        return thongBaoService.layThongBao(appSession.layMa());
    }

    @GetMapping("/chua-doc")
    public Map<String, Integer> demChuaDoc() {
        return Map.of("soLuong", thongBaoService.demChuaDoc(appSession.layMa()));
    }

    @PostMapping("/{ma}/da-doc")
    public Map<String, Boolean> danhDauDaDoc(@PathVariable int ma) {
        return Map.of("thanhCong", thongBaoService.danhDauDaDoc(ma, appSession.layMa()));
    }
}

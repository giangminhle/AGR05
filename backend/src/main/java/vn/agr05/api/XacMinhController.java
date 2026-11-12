package vn.agr05.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.agr05.bll.AppSession;
import vn.agr05.bll.XacMinhService;
import vn.agr05.entity.BaoCaoHienTruong;

/** UC05–UC10, UC18 (mục 3.3.3–3.3.5). Mã chuyên gia lấy từ AppSession. */
@RestController
@RequestMapping("/api/xac-minh")
public class XacMinhController {

    private final XacMinhService xacMinhService;
    private final AppSession appSession;

    public XacMinhController(XacMinhService xacMinhService, AppSession appSession) {
        this.xacMinhService = xacMinhService;
        this.appSession = appSession;
    }

    public record YeuCauDuyet(Integer maLoai, String khuyenNghi) {
    }

    public record YeuCauGhiChu(String lyDo, String ghiChu) {
    }

    @GetMapping("/cho")
    public List<BaoCaoHienTruong> layDanhSachCho() {
        return xacMinhService.layDanhSachCho(appSession.layMa());
    }

    @GetMapping("/da-xac-minh")
    public List<BaoCaoHienTruong> layDaXacMinh() {
        return xacMinhService.layDaXacMinh(appSession.layMa());
    }

    @GetMapping("/{ma}")
    public BaoCaoHienTruong layChiTiet(@PathVariable int ma) {
        return xacMinhService.layChiTiet(ma, appSession.layMa());
    }

    @PostMapping("/{ma}/duyet")
    public Map<String, Object> duyet(@PathVariable int ma, @RequestBody YeuCauDuyet yc) {
        boolean ok = xacMinhService.xacMinh(ma, yc.maLoai(), yc.khuyenNghi(), appSession.layMa());
        Map<String, Object> kq = new HashMap<>();
        kq.put("thanhCong", ok);
        kq.put("duBao", xacMinhService.layDuBao(ma));
        return kq;
    }

    @PostMapping("/{ma}/tu-choi")
    public Map<String, Boolean> tuChoi(@PathVariable int ma, @RequestBody YeuCauGhiChu yc) {
        return Map.of("thanhCong", xacMinhService.tuChoi(ma, yc.lyDo(), appSession.layMa()));
    }

    @PostMapping("/{ma}/da-xu-ly")
    public Map<String, Boolean> daXuLy(@PathVariable int ma, @RequestBody YeuCauGhiChu yc) {
        return Map.of("thanhCong", xacMinhService.danhDauDaXuLy(ma, yc.ghiChu(), appSession.layMa()));
    }
}

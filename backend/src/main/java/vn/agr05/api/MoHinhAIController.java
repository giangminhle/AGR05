package vn.agr05.api;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.agr05.bll.AIService;
import vn.agr05.entity.MoHinhAI;

/** UC19: huấn luyện lại và kích hoạt mô hình AI (mục 3.3.10). */
@RestController
@RequestMapping("/api/mo-hinh-ai")
public class MoHinhAIController {

    private final AIService aiService;

    public MoHinhAIController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping
    public Map<String, Object> layDanhSach() {
        Map<String, Object> kq = new HashMap<>();
        kq.put("dangSuDung", aiService.layDangSuDung());
        kq.put("dsPhienBan", aiService.layDanhSach());
        return kq;
    }

    @PostMapping("/huan-luyen")
    public MoHinhAI huanLuyenLai() {
        return aiService.huanLuyenLai();
    }

    @PostMapping("/{ma}/kich-hoat")
    public Map<String, Boolean> kichHoat(@PathVariable int ma) {
        return Map.of("thanhCong", aiService.kichHoat(ma));
    }
}

package vn.agr05.api;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.agr05.bll.ThamSoService;
import vn.agr05.entity.ThamSoHeThong;

/** UC16: cấu hình hệ thống. */
@RestController
@RequestMapping("/api/tham-so")
public class ThamSoController {

    private final ThamSoService thamSoService;

    public ThamSoController(ThamSoService thamSoService) {
        this.thamSoService = thamSoService;
    }

    public record YeuCauCapNhat(Integer giaTri) {
    }

    @GetMapping
    public List<ThamSoHeThong> layTatCa() {
        return thamSoService.layTatCa();
    }

    @PutMapping("/{ma}")
    public Map<String, Boolean> capNhat(@PathVariable String ma, @RequestBody YeuCauCapNhat yc) {
        int giaTri = yc.giaTri() == null ? 0 : yc.giaTri();
        return Map.of("thanhCong", thamSoService.capNhat(ma, giaTri));
    }
}

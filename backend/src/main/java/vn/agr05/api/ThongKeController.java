package vn.agr05.api;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.agr05.bll.BangThongKe;
import vn.agr05.bll.ThongKeService;

/** UC11. */
@RestController
public class ThongKeController {

    private final ThongKeService thongKeService;

    public ThongKeController(ThongKeService thongKeService) {
        this.thongKeService = thongKeService;
    }

    @GetMapping("/api/thong-ke")
    public BangThongKe thongKe(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tu,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate den,
                               @RequestParam(required = false) Integer maKhuVuc,
                               @RequestParam(required = false) Integer maLoai) {
        return thongKeService.thongKe(tu, den, maKhuVuc, maLoai);
    }
}

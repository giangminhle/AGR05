package vn.agr05.api;

import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.agr05.bll.AppSession;
import vn.agr05.bll.BaoCaoService;
import vn.agr05.entity.BaoCaoHienTruong;

/** UC02, UC03, UC04 (mục 3.3.2, 3.3.8). */
@RestController
@RequestMapping("/api/bao-cao")
public class BaoCaoController {

    private final BaoCaoService baoCaoService;
    private final AppSession appSession;

    public BaoCaoController(BaoCaoService baoCaoService, AppSession appSession) {
        this.baoCaoService = baoCaoService;
        this.appSession = appSession;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Integer> guiBaoCao(@ModelAttribute BaoCaoForm form,
                                          @RequestParam(name = "anh", required = false) List<MultipartFile> anh) {
        BaoCaoHienTruong bc = form.sangEntity();
        bc.setMaNguoiGui(appSession.layMa());
        int ma = baoCaoService.guiBaoCao(bc, MultipartTienIch.doi(anh));
        return Map.of("maBaoCao", ma);
    }

    @GetMapping("/cua-toi")
    public List<BaoCaoHienTruong> layCuaToi() {
        return baoCaoService.layCuaToi(appSession.layMa());
    }

    @GetMapping("/{ma}/de-sua")
    public BaoCaoHienTruong layDeSua(@PathVariable int ma) {
        return baoCaoService.layDeSua(ma, appSession.layMa());
    }

    @PutMapping(value = "/{ma}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Boolean> guiLai(@PathVariable int ma, @ModelAttribute BaoCaoForm form,
                                       @RequestParam(name = "anhMoi", required = false) List<MultipartFile> anhMoi,
                                       @RequestParam(name = "dsXoa", required = false) List<Integer> dsXoa) {
        BaoCaoHienTruong bc = form.sangEntity();
        bc.setMaBaoCao(ma);
        bc.setMaNguoiGui(appSession.layMa());
        return Map.of("thanhCong", baoCaoService.guiLai(bc, MultipartTienIch.doi(anhMoi), dsXoa));
    }
}

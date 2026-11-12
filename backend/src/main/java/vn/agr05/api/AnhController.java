package vn.agr05.api;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import vn.agr05.bll.AnhTai;
import vn.agr05.bll.AppSession;
import vn.agr05.bll.BaoCaoService;

/** Trả ảnh hiện trường có kiểm tra quyền xem (luật 10). */
@RestController
public class AnhController {

    private final BaoCaoService baoCaoService;
    private final AppSession appSession;

    public AnhController(BaoCaoService baoCaoService, AppSession appSession) {
        this.baoCaoService = baoCaoService;
        this.appSession = appSession;
    }

    @GetMapping("/api/anh/{maHinhAnh}")
    public ResponseEntity<byte[]> layAnh(@PathVariable int maHinhAnh) {
        AnhTai anh = baoCaoService.layAnh(maHinhAnh, appSession.getNguoiDung(), appSession.getDsMaKhuVuc());
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(anh.kieuNoiDung()))
                .cacheControl(CacheControl.noStore()).body(anh.noiDung());
    }
}

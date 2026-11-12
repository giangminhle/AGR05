package vn.agr05.api;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import vn.agr05.bll.FileTai;

/** Đổi MultipartFile của Spring Web sang FileTai của BLL. */
final class MultipartTienIch {

    private MultipartTienIch() {
    }

    static List<FileTai> doi(List<MultipartFile> ds) {
        List<FileTai> kq = new ArrayList<>();
        if (ds == null) {
            return kq;
        }
        for (MultipartFile f : ds) {
            if (f == null || f.isEmpty()) {
                continue;
            }
            try {
                kq.add(new FileTai(f.getOriginalFilename(), f.getSize(), f.getBytes()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return kq;
    }
}

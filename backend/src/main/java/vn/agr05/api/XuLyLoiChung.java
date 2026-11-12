package vn.agr05.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import vn.agr05.entity.NghiepVuException;

/** Đổi lỗi thành HTTP 400/403/404/409 kèm { "loi", "truong" }. */
@RestControllerAdvice
public class XuLyLoiChung {

    private static final Logger LOG = LoggerFactory.getLogger(XuLyLoiChung.class);

    @ExceptionHandler(NghiepVuException.class)
    public ResponseEntity<LoiApi> nghiepVu(NghiepVuException e) {
        return ResponseEntity.status(e.getLoai().getMaHttp()).body(new LoiApi(e.getMessage(), e.getTruong()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<LoiApi> rangBuoc(BindException e) {
        String truong = e.getFieldError() == null ? null : e.getFieldError().getField();
        return ResponseEntity.badRequest().body(new LoiApi("Giá trị không hợp lệ ở ô " + truong + ".", truong));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class})
    public ResponseEntity<LoiApi> sai(Exception e) {
        return ResponseEntity.badRequest().body(new LoiApi("Dữ liệu gửi lên không hợp lệ.", null));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<LoiApi> fileQuaLon(MaxUploadSizeExceededException e) {
        return ResponseEntity.badRequest().body(new LoiApi("Ảnh vượt quá dung lượng cho phép (5 MB mỗi ảnh).", "anh"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<LoiApi> khongCo(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new LoiApi("Không tìm thấy đường dẫn.", null));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<LoiApi> rangBuocCsdl(DataIntegrityViolationException e) {
        LOG.warn("Vi phạm ràng buộc CSDL: {}", e.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new LoiApi("Dữ liệu vi phạm ràng buộc của hệ thống.", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<LoiApi> khac(Exception e) {
        LOG.error("Lỗi không lường trước", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new LoiApi("Lỗi hệ thống, vui lòng thử lại sau.", null));
    }
}

package vn.agr05.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
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

    /** Sai kiểu nội dung (ví dụ gửi form thường cho API cần multipart) hoặc sai phương thức: lỗi phía máy khách, không phải 500. */
    @ExceptionHandler({HttpMediaTypeNotSupportedException.class, HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<LoiApi> saiYeuCau(Exception e) {
        HttpStatus ma = e instanceof HttpMediaTypeNotSupportedException ? HttpStatus.UNSUPPORTED_MEDIA_TYPE
                : HttpStatus.METHOD_NOT_ALLOWED;
        return ResponseEntity.status(ma).body(new LoiApi("Yêu cầu gửi lên không đúng định dạng.", null));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<LoiApi> fileQuaLon(MaxUploadSizeExceededException e) {
        return ResponseEntity.badRequest().body(new LoiApi("Ảnh vượt quá dung lượng cho phép (5 MB mỗi ảnh).", "anh"));
    }

    private static final Resource TRANG_REACT = new ClassPathResource("static/index.html");

    /**
     * Đường dẫn không có: nếu là API thì trả 404 JSON. Nếu là đường dẫn của giao diện (ví dụ /xac-minh khi bấm F5)
     * và giao diện đã được đóng gói vào jar thì trả index.html để React Router hiển thị đúng trang.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> khongCo(NoResourceFoundException e, HttpServletRequest req) {
        String duongDan = req.getRequestURI();
        boolean laTrangGiaoDien = !duongDan.startsWith("/api/") && !duongDan.substring(duongDan.lastIndexOf('/') + 1).contains(".");
        if (laTrangGiaoDien && TRANG_REACT.exists()) {
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(TRANG_REACT);
        }
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

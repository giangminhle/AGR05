package vn.agr05.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.agr05.bll.AccountService;
import vn.agr05.bll.AppSession;
import vn.agr05.entity.NguoiDung;

/** UC01: đăng nhập, đăng xuất, thông tin người đang đăng nhập (mục 3.3.1). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountService accountService;
    private final AppSession appSession;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AccountService accountService, AppSession appSession,
                          SecurityContextRepository securityContextRepository) {
        this.accountService = accountService;
        this.appSession = appSession;
        this.securityContextRepository = securityContextRepository;
    }

    public record YeuCauDangNhap(String tenDangNhap, String matKhau) {
    }

    @PostMapping("/dang-nhap")
    public NguoiDungDto dangNhap(@RequestBody YeuCauDangNhap yc, HttpServletRequest req, HttpServletResponse res) {
        // Đổi mã phiên trước khi đăng nhập để chống chiếm phiên (session fixation)
        req.getSession(true);
        req.changeSessionId();
        NguoiDung nd = accountService.dangNhap(yc.tenDangNhap(), yc.matKhau());

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(nd.getMaNguoiDung(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + nd.getVaiTro())));
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
        securityContextRepository.saveContext(ctx, req, res);
        return NguoiDungDto.tu(nd, appSession.getDsMaKhuVuc());
    }

    @PostMapping("/dang-xuat")
    public ResponseEntity<Void> dangXuat(HttpServletRequest req) {
        appSession.xoa();
        HttpSession s = req.getSession(false);
        if (s != null) {
            s.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/toi")
    public NguoiDungDto toi() {
        return NguoiDungDto.tu(appSession.getNguoiDung(), appSession.getDsMaKhuVuc());
    }
}

package vn.agr05.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.agr05.bll.AccountService;

/**
 * Mỗi lần gọi API: tài khoản trong phiên đã bị khóa hoặc bị đổi vai trò thì hủy phiên, trả 401 để giao diện
 * đưa về trang đăng nhập. Không có bước này, người bị khóa vẫn dùng được phiên cũ tới khi hết hạn (8 giờ).
 */
@Component
public class KiemTraPhienFilter extends OncePerRequestFilter {

    private final AccountService accountService;

    public KiemTraPhienFilter(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String uri = req.getRequestURI();
        return !uri.startsWith("/api/") || uri.equals("/api/auth/dang-nhap") || uri.equals("/api/auth/dang-ky");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        if (req.getSession(false) != null && !accountService.phienConHieuLuc()) {
            HttpSession s = req.getSession(false);
            if (s != null) {
                s.invalidate();
            }
            SecurityContextHolder.clearContext();
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"loi\":\"Tài khoản đã bị khóa hoặc thay đổi quyền, vui lòng đăng nhập lại.\",\"truong\":null}");
            return;
        }
        chain.doFilter(req, res);
    }
}

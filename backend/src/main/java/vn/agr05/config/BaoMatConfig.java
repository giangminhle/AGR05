package vn.agr05.config;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Spring Security: đăng nhập bằng phiên (session cookie), phân quyền theo vai trò ở URL (bảng API docs/architecture.md).
 * Quyền chi tiết (khu vực, chủ báo cáo) do Service kiểm tra (BR05).
 */
@Configuration
public class BaoMatConfig {

    private static final String NONG_DAN = "NONG_DAN";
    private static final String CHUYEN_GIA = "CHUYEN_GIA";
    private static final String QUAN_TRI = "QUAN_TRI";

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository repo) throws Exception {
        http
            // Giao diện React gọi API cùng nguồn (qua proxy của Vite), cookie phiên SameSite=Lax; đồ án không bật CSRF token
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .securityContext(c -> c.securityContextRepository(repo))
            .authorizeHttpRequests(a -> a
                .requestMatchers(HttpMethod.POST, "/api/auth/dang-nhap").permitAll()
                .requestMatchers("/api/bao-cao/**").hasRole(NONG_DAN)
                .requestMatchers("/api/xac-minh/**").hasRole(CHUYEN_GIA)
                .requestMatchers("/api/thong-ke/**").hasAnyRole(CHUYEN_GIA, QUAN_TRI)
                .requestMatchers(HttpMethod.GET, "/api/loai-sau-benh/**", "/api/khu-vuc").authenticated()
                .requestMatchers("/api/loai-sau-benh/**", "/api/khu-vuc/**", "/api/nguoi-dung/**", "/api/tham-so/**",
                        "/api/mo-hinh-ai/**").hasRole(QUAN_TRI)
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> ghiLoi(res, 401, "Vui lòng đăng nhập."))
                .accessDeniedHandler((req, res, ex) -> ghiLoi(res, 403, "Bạn không có quyền dùng chức năng này.")));
        return http.build();
    }

    private static void ghiLoi(HttpServletResponse res, int ma, String loi) throws IOException {
        res.setStatus(ma);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"loi\":\"" + loi + "\",\"truong\":null}");
    }
}

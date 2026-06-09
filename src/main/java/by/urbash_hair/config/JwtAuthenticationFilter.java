package by.urbash_hair.config;

import by.urbash_hair.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();

        // Пропускаем только реально публичные эндпоинты без проверки JWT.
        // Важно: /api/appointments/** в целом должен требовать JWT (кроме GET /api/appointments/available).
        if (isPublicEndpoint(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }


        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        try {
            System.err.println("=== AUTH DEBUG ===");
            System.err.println("Authorization header present: true");
            System.err.println("jwt length: " + (jwt != null ? jwt.length() : 0));
            System.err.println("=== AUTH DEBUG END ===");

            Long userId = jwtService.extractUserId(jwt);


            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserById(userId);
                String role = jwtService.extractRole(jwt);
                List<GrantedAuthority> authorities = new ArrayList<>();
                if (role != null && !role.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }

                System.err.println("=== BOOK DEBUG ===");
                System.err.println("Path: " + request.getServletPath());
                System.err.println("JWT role claim: " + role);
                System.err.println("authorities: " + authorities);
                System.err.println("userId: " + userId);
                System.err.println("===================");

                // Fallback: если claim role не пришёл или пустой — оставляем authorities пустыми.
                // В этом проекте роль должна быть в claim "role" (JwtService.generateToken).


                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(

                        userDetails,
                        null,
                        authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Невалидный токен – просто продолжаем без аутентификации
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path, String method) {
        // Публичный только GET /api/appointments/available
        if (HttpMethod.GET.matches(method) && "/api/appointments/available".equals(path)) {
            return true;
        }

        // Статические ресурсы и публичные страницы
        return path.equals("/") ||
                path.endsWith(".html") ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.endsWith(".png") ||
                path.endsWith(".jpg") ||
                path.endsWith(".jpeg") ||
                path.endsWith(".gif") ||
                path.endsWith(".ico") ||
                path.startsWith("/images/") ||
                path.startsWith("/videos/") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/api/auth/") ||
                path.startsWith("/api/posts") ||
                path.startsWith("/api/reviews") ||
                path.startsWith("/api/services") ||
                path.startsWith("/api/masters") ||
                path.startsWith("/api/course-applications") ||
                path.startsWith("/api/job-applications");
    }

}
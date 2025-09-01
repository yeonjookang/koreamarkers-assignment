package org.example.assignment.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.assignment.domain.user.entity.User;
import org.example.assignment.domain.user.repository.UserRepository;
import org.example.assignment.global.exception.BizException;
import org.example.assignment.global.exception.ErrorDescription;
import org.example.assignment.global.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain filterChain) throws IOException, ServletException {

        String path = httpRequest.getRequestURI();
        
        // 인증이 필요 없는 경로들
        if ("/signup".equals(path) || "/signin".equals(path) || "/".equals(path) || "/login".equals(path) || "/dashboard".equals(path) || "/user-info".equals(path) || path.startsWith("/css/") || path.startsWith("/js/")) {
            filterChain.doFilter(httpRequest, httpResponse);
            return;
        }

        // 인증이 필요한 경로들 (API 엔드포인트)
        String bearerJwt = httpRequest.getHeader("Authorization");

        if (bearerJwt == null || !bearerJwt.startsWith("Bearer ")) {
            setErrorResponse(httpResponse, "JWT 토큰이 필요합니다.");
            return;
        }

        String jwt = jwtUtil.substringToken(bearerJwt);

        try {
            if (jwtUtil.validateToken(jwt)) {
                Long userId = jwtUtil.getUserIdFromToken(jwt);
                User user = userRepository.findById(userId).orElseThrow(() -> {
                    throw new BizException(ErrorDescription.NOT_FOUND_USER);
                });

                Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (BizException e) {
            setErrorResponse(httpResponse, e.getMessage());
            return;
        }

        filterChain.doFilter(httpRequest, httpResponse);
    }

    private void setErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String body = new ObjectMapper().writeValueAsString(
                Map.of(
                        "status", 401,
                        "error", "Unauthorized",
                        "message", message
                )
        );
        response.getWriter().write(body);
    }
}

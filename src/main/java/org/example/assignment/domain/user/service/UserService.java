package org.example.assignment.domain.user.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.assignment.domain.user.dto.request.SignInRequest;
import org.example.assignment.domain.user.dto.request.SignUpRequest;
import org.example.assignment.domain.user.dto.response.SignInResponse;
import org.example.assignment.domain.user.entity.User;
import org.example.assignment.domain.user.repository.UserRepository;
import org.example.assignment.global.exception.BizException;
import org.example.assignment.global.exception.ErrorDescription;
import org.example.assignment.global.util.JwtUtil;
import org.example.assignment.global.util.PasswordEncoder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    public SignInResponse signIn(SignInRequest request, HttpServletResponse httpServletResponse) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BizException(ErrorDescription.NOT_FOUND_USER));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BizException(ErrorDescription.INVALID_PASSWORD);
        }
        String accessToken = jwtUtil.createAccessToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user);

        redisTemplate.opsForValue().set(
                user.getEmail(),
                refreshToken
        );

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        httpServletResponse.addCookie(cookie);

        return new SignInResponse(accessToken);
    }

    public void signUp(SignUpRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BizException(ErrorDescription.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .email(request.email())
                .password(encodedPassword)
                .build();

        userRepository.save(user);
    }

    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        redisTemplate.delete(user.getEmail());
    }

    public SignInResponse reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            throw new BizException(ErrorDescription.NOT_FOUND_ACCESS_TOKEN);
        }

        String token = jwtUtil.substringToken(refreshToken);
        if (!jwtUtil.validateToken(token)) {
            throw new BizException(ErrorDescription.INVALID_TOKEN);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorDescription.NOT_FOUND_USER));

        String newAccessToken = jwtUtil.createAccessToken(user);
        String newRefreshToken = jwtUtil.createRefreshToken(user);

        redisTemplate.opsForValue().set(
                user.getEmail(),
                newRefreshToken
        );

        Cookie cookie = new Cookie("refreshToken", newRefreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        return new SignInResponse(newAccessToken);
    }

}

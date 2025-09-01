package org.example.assignment.domain.user.service;

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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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

}

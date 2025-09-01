package org.example.assignment.domain.user.dto.request;

public record SignUpRequest(
        String email,
        String password,
        String nickname
) {
}

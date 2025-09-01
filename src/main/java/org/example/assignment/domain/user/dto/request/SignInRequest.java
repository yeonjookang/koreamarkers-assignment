package org.example.assignment.domain.user.dto.request;

public record SignInRequest(
        String email,
        String password
) {
}

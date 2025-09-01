package org.example.assignment.global.exception;

import lombok.Getter;

@Getter
public enum ErrorDescription {
    NOT_FOUND_ACCESS_TOKEN("Access Token이 존재하지 않습니다."),
    TOKEN_EXPIRED("만료된 토큰입니다."),
    INVALID_TOKEN("유효하지 않은 토큰입니다."),
    NOT_FOUND_USER("존재하지 않은 유저입니다."),
    INVALID_PASSWORD("비밀번호가 유효하지 않습니다."),
    DUPLICATE_EMAIL("이미 가입된 이메일입니다.");

    private final String message;

    ErrorDescription(String message) {
        this.message = message;
    }
}

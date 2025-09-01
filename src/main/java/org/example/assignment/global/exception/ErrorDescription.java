package org.example.assignment.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorDescription implements ErrorCode{
    NOT_FOUND_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED,"Access Token이 존재하지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,"만료된 토큰입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,"유효하지 않은 토큰입니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND,"존재하지 않은 유저입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 유효하지 않습니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST,"이미 가입된 이메일입니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}

package org.example.assignment.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ErrorResponse> handleBizError(BizException exception) {
        log.info(exception.getMessage());
        return ResponseEntity
                .status(exception.getErrorCode().getStatus())
                .body(ErrorResponse.of(exception.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException exception) {
        log.info(exception.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(ErrorDescription.INVALID_INPUT_VALUE));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception) {
        log.info(exception.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(ErrorDescription.INVALID_INPUT_VALUE));
    }
}

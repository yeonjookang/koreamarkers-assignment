package org.example.assignment.global.exception;

public class BizException extends RuntimeException {

    public BizException(ErrorDescription errorDescription) {
        super(errorDescription.getMessage());
    }

    public BizException(String message) {
        super(message);
    }
}

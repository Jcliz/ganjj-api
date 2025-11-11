package com.ganjj.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends BusinessException {
    
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public ValidationException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
    
    public ValidationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public ValidationException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}

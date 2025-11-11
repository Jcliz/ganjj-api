package com.ganjj.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends BusinessException {
    
    public ConflictException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public ConflictException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
    
    public ConflictException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public ConflictException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}

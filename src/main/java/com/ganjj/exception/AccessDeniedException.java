package com.ganjj.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends BusinessException {
    
    public AccessDeniedException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public AccessDeniedException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
    
    public AccessDeniedException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public AccessDeniedException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}

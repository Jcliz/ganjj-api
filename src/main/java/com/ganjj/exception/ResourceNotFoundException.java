package com.ganjj.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public ResourceNotFoundException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
    
    public ResourceNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public ResourceNotFoundException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}

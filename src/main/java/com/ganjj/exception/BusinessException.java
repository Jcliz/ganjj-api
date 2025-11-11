package com.ganjj.exception;

public class BusinessException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final Object[] args;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = null;
    }
    
    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode.formatMessage(args));
        this.errorCode = errorCode;
        this.args = args;
    }
    
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.args = null;
    }
    
    public BusinessException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode.formatMessage(args), cause);
        this.errorCode = errorCode;
        this.args = args;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    public String getCode() {
        return errorCode.getCode();
    }
    
    public Object[] getArgs() {
        return args;
    }
    
    public String getFormattedMessage() {
        if (args != null && args.length > 0) {
            return errorCode.formatMessage(args);
        }
        return errorCode.getMessage();
    }
}

package com.ganjj.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        logger.warn("Business exception: {} - {}", ex.getCode(), ex.getMessage());
        
        HttpStatus status = getHttpStatusFromException(ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getCode(),
                ex.getFormattedMessage(),
                request.getRequestURI(),
                status.value()
        );
        
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        
        logger.warn("Resource not found: {} - {}", ex.getCode(), ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getCode(),
                ex.getFormattedMessage(),
                request.getRequestURI(),
                HttpStatus.NOT_FOUND.value()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        
        logger.warn("Validation exception: {} - {}", ex.getCode(), ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getCode(),
                ex.getFormattedMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            ConflictException ex, HttpServletRequest request) {
        
        logger.warn("Conflict exception: {} - {}", ex.getCode(), ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getCode(),
                ex.getFormattedMessage(),
                request.getRequestURI(),
                HttpStatus.CONFLICT.value()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        
        logger.warn("Access denied: {} - {}", ex.getCode(), ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getCode(),
                ex.getFormattedMessage(),
                request.getRequestURI(),
                HttpStatus.FORBIDDEN.value()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        logger.warn("Validation error: {}", ex.getMessage());
        
        List<ErrorResponse.ValidationError> validationErrors = new ArrayList<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            Object rejectedValue = ((FieldError) error).getRejectedValue();
            validationErrors.add(new ErrorResponse.ValidationError(fieldName, message, rejectedValue));
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.GENERAL_VALIDATION_ERROR.getCode(),
                ErrorCode.GENERAL_VALIDATION_ERROR.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );
        errorResponse.setErrors(validationErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex, HttpServletRequest request) {
        
        logger.warn("Entity not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                "ERR_RESOURCE_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.NOT_FOUND.value()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            Exception ex, HttpServletRequest request) {
        
        logger.warn("Authentication error: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.AUTH_INVALID_CREDENTIALS.getCode(),
                ErrorCode.AUTH_INVALID_CREDENTIALS.getMessage(),
                request.getRequestURI(),
                HttpStatus.UNAUTHORIZED.value()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(
            SecurityException ex, HttpServletRequest request) {
        
        logger.warn("Security exception: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.AUTH_ACCESS_DENIED.getCode(),
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.FORBIDDEN.value()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleSpringAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
        
        logger.warn("Spring Security access denied: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.AUTH_ACCESS_DENIED.getCode(),
                "Você não tem permissão para acessar este recurso. Apenas administradores podem realizar esta ação.",
                request.getRequestURI(),
                HttpStatus.FORBIDDEN.value()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        logger.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.GENERAL_BAD_REQUEST.getCode(),
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, HttpServletRequest request) {
        
        logger.warn("Illegal state: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.GENERAL_BAD_REQUEST.getCode(),
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.CONFLICT.value()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        logger.warn("Type mismatch: {}", ex.getMessage());
        
        String message = String.format("O parâmetro '%s' deve ser do tipo %s", 
                ex.getName(), ex.getRequiredType().getSimpleName());
        
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.GENERAL_BAD_REQUEST.getCode(),
                message,
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        logger.warn("Message not readable: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.GENERAL_BAD_REQUEST.getCode(),
                "Corpo da requisição inválido ou malformado",
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        
        logger.warn("Method not supported: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.GENERAL_METHOD_NOT_ALLOWED.getCode(),
                String.format("Método %s não é suportado para esta rota", ex.getMethod()),
                request.getRequestURI(),
                HttpStatus.METHOD_NOT_ALLOWED.value()
        );
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        logger.error("Unexpected error occurred: ", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.GENERAL_INTERNAL_ERROR.getCode(),
                ErrorCode.GENERAL_INTERNAL_ERROR.getMessage(),
                request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    private HttpStatus getHttpStatusFromException(BusinessException ex) {
        if (ex instanceof ResourceNotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else if (ex instanceof ValidationException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof ConflictException) {
            return HttpStatus.CONFLICT;
        } else if (ex instanceof AccessDeniedException) {
            return HttpStatus.FORBIDDEN;
        }
        return HttpStatus.BAD_REQUEST;
    }
}

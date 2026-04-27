package com.recruit.c360.exception;
import com.recruit.c360.dto.response.ApiResponse;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> notFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<?>> badRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> validation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ApiResponse.error(msg));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> generic(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Internal error: " + ex.getMessage()));
    }
}

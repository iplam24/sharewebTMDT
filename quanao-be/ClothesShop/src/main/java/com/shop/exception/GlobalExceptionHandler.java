package com.shop.exception;

import com.shop.dto.response.ErrorResponseDTO;
import com.shop.dto.response.ValidationErrorResponseDTO;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ====== 1. Lỗi validate body: @Valid + @NotBlank, @NotNull,... ======
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String fieldName = fieldError.getField();
            String errorMessage = fieldError.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        }

        String path = request.getDescription(false); // "uri=/api/products"
        ValidationErrorResponseDTO body = new ValidationErrorResponseDTO(
                new Date(),
                "Dữ liệu không hợp lệ",
                path,
                errors
        );

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // ====== 2. Lỗi validate trên @RequestParam, @PathVariable,... (@Validated + ConstraintViolationException) ======
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv -> {
            String path = cv.getPropertyPath().toString(); // ví dụ: "createProduct.requestDTO.name"
            String field = path.substring(path.lastIndexOf('.') + 1);
            errors.put(field, cv.getMessage());
        });

        String path = request.getDescription(false);
        ValidationErrorResponseDTO body = new ValidationErrorResponseDTO(
                new Date(),
                "Dữ liệu không hợp lệ",
                path,
                errors
        );

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // ====== 3. Lỗi không tìm thấy resource ======
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {

        ErrorResponseDTO error = new ErrorResponseDTO(
                new Date(),
                ex.getMessage(),
                request.getDescription(false)
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // ====== 4. Lỗi sai kiểu tham số: /api/products/abc (đáng lẽ phải là Long) ======
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest request) {

        String msg = String.format("Giá trị '%s' không hợp lệ cho tham số '%s'",
                ex.getValue(), ex.getName());

        ErrorResponseDTO error = new ErrorResponseDTO(
                new Date(),
                msg,
                request.getDescription(false)
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ====== 5. Xử lý RuntimeException (bao gồm lỗi mã giảm giá) ======
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuntimeException(
            RuntimeException ex,
            WebRequest request) {

        ErrorResponseDTO error = new ErrorResponseDTO(
                new Date(),
                ex.getMessage(), // Lấy message từ RuntimeException (ví dụ: "Bạn đã sử dụng mã giảm giá này rồi")
                request.getDescription(false)
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST); // Trả về 400 Bad Request
    }

    // ====== 6. Bắt tất cả lỗi còn lại (Internal Server Error) ======
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAllExceptions(
            Exception ex,
            WebRequest request) {

        ErrorResponseDTO error = new ErrorResponseDTO(
                new Date(),
                "Đã có lỗi xảy ra trong hệ thống",
                request.getDescription(false)
        );

        // Anh có thể log chi tiết ex tại đây (log.error)
        ex.printStackTrace();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

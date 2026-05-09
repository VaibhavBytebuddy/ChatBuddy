package com.om.ChatBuddy.common.exception;

import com.om.ChatBuddy.common.constant.ErrorCode;
import com.om.ChatBuddy.common.model.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex) {
        log.error("Application Exception: {} | Dev Message: {}", ex.getMessage(), ex.getDeveloperMessage());
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode.getCode(), ex.getMessage(), ex.getDeveloperMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("Unexpected Exception: ", ex);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage(), ex.getMessage()));
    }
}

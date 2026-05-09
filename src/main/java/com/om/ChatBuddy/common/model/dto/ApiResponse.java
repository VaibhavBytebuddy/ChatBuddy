package com.om.ChatBuddy.common.model.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    boolean success;
    String message;
    String developerMessage;
    T data;
    String errorCode;
    
    @Builder.Default
    LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String errorCode, String message, String developerMessage) {
        return ApiResponse.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .developerMessage(developerMessage)
                .build();
    }
}

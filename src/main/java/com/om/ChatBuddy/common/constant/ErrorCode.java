package com.om.ChatBuddy.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR("ERR_000", "An unexpected error occurred", "Check server logs for details", HttpStatus.INTERNAL_SERVER_ERROR),
    ROOM_NOT_FOUND("ERR_001", "Chat room not found", "The requested room ID does not exist in MongoDB", HttpStatus.NOT_FOUND),
    INVALID_MESSAGE("ERR_002", "Message content is invalid", "Message body cannot be empty or exceed max length", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_ACCESS("ERR_003", "You are not authorized", "Security context missing or insufficient permissions", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final String developerMessage;
    private final HttpStatus httpStatus;
}

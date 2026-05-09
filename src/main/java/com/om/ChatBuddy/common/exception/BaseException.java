package com.om.ChatBuddy.common.exception;

import com.om.ChatBuddy.common.constant.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BaseException extends RuntimeException {

    ErrorCode errorCode;
    String developerMessage;

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.developerMessage = errorCode.getDeveloperMessage();
    }

    public BaseException(ErrorCode errorCode, String developerMessage) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.developerMessage = developerMessage;
    }
}

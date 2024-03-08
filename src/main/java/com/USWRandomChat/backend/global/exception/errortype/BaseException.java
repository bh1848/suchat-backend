package com.USWRandomChat.backend.global.exception.errortype;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final ExceptionType exceptionType;

    public BaseException(ExceptionType exceptionType) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
        this.exceptionType = null;
    }
}
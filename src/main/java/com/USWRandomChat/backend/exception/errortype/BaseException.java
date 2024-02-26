package com.USWRandomChat.backend.exception.errortype;

import com.USWRandomChat.backend.exception.ExceptionType;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final ExceptionType exceptionType;

    public BaseException(ExceptionType exceptionType) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
    }
}
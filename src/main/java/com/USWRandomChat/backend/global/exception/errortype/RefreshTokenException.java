package com.USWRandomChat.backend.global.exception.errortype;

import com.USWRandomChat.backend.global.exception.ExceptionType;

public class RefreshTokenException extends BaseException{
    public RefreshTokenException(ExceptionType exceptionType) {
        super(exceptionType);
    }
}

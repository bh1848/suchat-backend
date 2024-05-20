package com.USWRandomChat.backend.global.exception.errortype;

import com.USWRandomChat.backend.global.exception.ExceptionType;

public class AccessTokenException extends BaseException{
    public AccessTokenException(ExceptionType exceptionType) {
        super(exceptionType);
    }
}

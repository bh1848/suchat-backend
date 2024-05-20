package com.USWRandomChat.backend.global.exception.errortype;

import com.USWRandomChat.backend.global.exception.ExceptionType;

public class TokenException extends BaseException{
    public TokenException(ExceptionType exceptionType) {
        super(exceptionType);
    }
}


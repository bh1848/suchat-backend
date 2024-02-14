package com.USWRandomChat.backend.exception.errortype;

import com.USWRandomChat.backend.exception.ExceptionType;

public class TokenException extends BaseException {

    public TokenException(ExceptionType exceptionType) {
        super(exceptionType);
    }
}
package com.USWRandomChat.backend.exception.errortype;

import com.USWRandomChat.backend.exception.ExceptionType;

public class ProfileException extends BaseException{
    public ProfileException(ExceptionType exceptionType) {
        super(exceptionType);
    }
}

package com.USWRandomChat.backend.global.exception.errortype;


import com.USWRandomChat.backend.global.exception.ExceptionType;

public class AccountException extends BaseException {

    public AccountException(ExceptionType exceptionType) {
        super(exceptionType);
    }
}
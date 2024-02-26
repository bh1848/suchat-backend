package com.USWRandomChat.backend.global.exception.errortype;

import com.USWRandomChat.backend.global.exception.ExceptionType;

public class CodeException extends BaseException{
    public CodeException(ExceptionType exceptionType) {

        super(exceptionType);
    }
}

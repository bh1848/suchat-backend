package com.USWRandomChat.backend.exception.errortype;

import com.USWRandomChat.backend.exception.ExceptionType;

public class CodeException extends BaseException{
    public CodeException(ExceptionType exceptionType) {

        super(exceptionType);
    }
}

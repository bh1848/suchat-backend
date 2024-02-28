package com.USWRandomChat.backend.global.exception.errortype;

import com.USWRandomChat.backend.global.exception.ExceptionType;

public class ChatException extends BaseException{
    public ChatException(ExceptionType exceptionType) {
        super(exceptionType);
    }
}

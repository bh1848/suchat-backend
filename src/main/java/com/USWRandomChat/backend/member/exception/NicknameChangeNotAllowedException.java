package com.USWRandomChat.backend.member.exception;

public class NicknameChangeNotAllowedException extends RuntimeException{
    public NicknameChangeNotAllowedException(String message){
        super(message);
    }
}

package com.USWRandomChat.backend.member.exception;

public class CheckDuplicateNicknameException extends RuntimeException {
    public CheckDuplicateNicknameException(String message) {
        super(message);
    }
}

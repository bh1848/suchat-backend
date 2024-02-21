package com.USWRandomChat.backend.member.exception;

public class CheckDuplicateEmailException extends RuntimeException {
    public CheckDuplicateEmailException(String message) {
        super(message);
    }
}

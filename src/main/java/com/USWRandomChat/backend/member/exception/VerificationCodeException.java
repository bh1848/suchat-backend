package com.USWRandomChat.backend.member.exception;

import lombok.Getter;

@Getter
public class VerificationCodeException extends RuntimeException {
    public VerificationCodeException(String message) {
        super(message);
    }
}

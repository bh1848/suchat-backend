package com.USWRandomChat.backend.emailAuth.exception;

import lombok.Getter;

@Getter
public class VerificationCodeException extends RuntimeException {
    public VerificationCodeException(String message) {
        super(message);
    }
}

package com.USWRandomChat.backend.emailAuth.exception;

import lombok.Getter;

@Getter
public class VerificationCodeException extends RuntimeException {

    private final String errorMessage;

    public VerificationCodeException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }
}

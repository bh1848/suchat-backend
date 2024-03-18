package com.USWRandomChat.backend.global.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiResponse {
    // Getter와 Setter
    private String message;

    public ApiResponse(String message) {
        this.message = message;
    }
}

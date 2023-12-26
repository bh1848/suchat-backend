package com.USWRandomChat.backend.response;

import lombok.Getter;

@Getter
public class FormResponse {
    boolean success;
    int code;
    String message;
}

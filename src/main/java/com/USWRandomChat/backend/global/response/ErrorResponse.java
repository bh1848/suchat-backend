package com.USWRandomChat.backend.global.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {

    private String exception;

    private String code;

    private String message;

    private Integer status;

    private String error;

}

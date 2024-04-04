package com.USWRandomChat.backend.member.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SignInRequest {

    private String account;
    private String password;
}

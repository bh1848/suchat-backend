package com.USWRandomChat.backend.global.response;

import lombok.Getter;

@Getter
public class SingleResponse<T> extends FormResponse {
    T data;
}

package com.USWRandomChat.backend.response;

import lombok.Getter;

@Getter
public class SingleResponse<T> extends FormResponse {
    T data;
}

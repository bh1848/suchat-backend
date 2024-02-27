package com.USWRandomChat.backend.global.response;

import lombok.Getter;

import java.util.List;

@Getter
public class ListResponse <T> extends FormResponse{
    List<T> dataList;
}

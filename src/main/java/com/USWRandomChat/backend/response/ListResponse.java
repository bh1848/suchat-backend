package com.USWRandomChat.backend.response;

import lombok.Getter;

import java.util.List;

@Getter
public class ListResponse <T> extends FormResponse{
    List<T> dataList;
}

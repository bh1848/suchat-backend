package com.USWRandomChat.backend.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MessageResponse {
    //채팅방 속 하나의 메시지
    private int messageNumber;
    private String sender;
    private String contents;
    private LocalDateTime sendTime;
}

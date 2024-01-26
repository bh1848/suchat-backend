package com.USWRandomChat.backend.chat.chatDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {

    public enum MessageType{
        //입장, 채팅, 퇴장
        ENTER, TALK, QUIT
    }
    //메시지 타입
    private MessageType type;

    //방 번호
    private String roomId;
    //발신자
    private String sender;
    //메시지
    private String message;
}

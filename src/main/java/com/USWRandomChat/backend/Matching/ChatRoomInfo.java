package com.USWRandomChat.backend.Matching;

public class ChatRoomInfo {
    private final Participant participant1;
    private final Participant participant2;
    private final String chatRoomUuid;

    public ChatRoomInfo(Participant participant1, Participant participant2, String chatRoomUuid) {
        this.participant1 = participant1;
        this.participant2 = participant2;
        this.chatRoomUuid = chatRoomUuid;
    }
}

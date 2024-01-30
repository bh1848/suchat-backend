package com.USWRandomChat.backend.Matching;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@Entity
@Getter
@Builder
public class MatchingParticipants {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String participant1;

    private String participant2;

    private String chatRoomId;

    public MatchingParticipants() {

    }

    public static MatchingParticipants createMatchingParticipants(String user1, String user2, String chatRoomId) {
        return MatchingParticipants.builder()
                .participant1(user1)
                .participant2(user2)
                .chatRoomId(chatRoomId)
                .build();
    }

    public static MatchingParticipants createPair(String user1, String user2){
        return MatchingParticipants.builder()
                .participant1(user1)
                .participant2(user2)
                .build();
    }
}

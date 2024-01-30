package com.USWRandomChat.backend.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "matchParticipants_table")
public class MatchParticipants {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String participant1;

    private String participant2;

    private String chatRoomId;

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public static MatchParticipants createPair(String user1, String user2){
        return MatchParticipants.builder()
                .participant1(user1)
                .participant2(user2)
                .build();
    }

}

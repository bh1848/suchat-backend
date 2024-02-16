package com.USWRandomChat.backend.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Message_table")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MESSAGE_ID")
    private Long id;

    @Column(name = "ROOM_ID")
    private String roomId;

    private String sender;

    @Column(name = "CONTENTS")
    private String contents;

    @Column(name = "SEND_TIME")
    private LocalDateTime sendTime;

    @Column(name = "MESSAGE_NUMBER")
    private int messageNumber;
}
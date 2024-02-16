package com.USWRandomChat.backend.chat.domain;

import com.USWRandomChat.backend.member.domain.Member;
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

    @Column(name = "MESSAGE_NUMBER")
    private int messageNumber;

    @Column(name = "ROOOM_ID")
    private String roomId;

    //불변하는 account 값으로 설정
    @Column(name = "SENDER")
    private String sender;

    @Column(name = "CONTENTS", nullable = false)
    private String contents;

    @Column(name = "SEND_TIME", nullable = false)
    private LocalDateTime sendTime;
}

package com.USWRandomChat.backend.chat.service;

import com.USWRandomChat.backend.chat.domain.Message;
import com.USWRandomChat.backend.chat.dto.MessageRequest;
import com.USWRandomChat.backend.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final MessageRepository messageRepository;

    public void saveMessage(MessageRequest messageRequest, String roomId) {

        /*
        * 채팅방에 따라 독립적인 messageNumber 증가
        * */
        int lastMessageNumber = messageRepository.findTopByRoomIdOrderByMessageNumberDesc(roomId)
                .map(Message::getMessageNumber)
                .orElse(0);

        //messageNumber 증가 후 메시지 저장
        Message message = Message
                .builder()
                .roomId(roomId)
                .messageNumber(lastMessageNumber + 1)
                .sender(messageRequest.getSender())
                .contents(messageRequest.getContents())
                .sendTime(LocalDateTime.now())
                .build();

        messageRepository.save(message);
        log.info("메시지 저장");
    }
}

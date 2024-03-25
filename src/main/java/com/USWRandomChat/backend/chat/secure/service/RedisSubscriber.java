package com.USWRandomChat.backend.chat.secure.service;

import com.USWRandomChat.backend.chat.domain.PubMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    /*
     * Redis에서 메시지가 발행(publish)되면 대기하고 있던 onMessage가 해당 메시지를 받아 처리한다.
     * */

    public void sendMessage(String publishMessage) {
        try {
            log.info("publish 전 message: {}", publishMessage);

            PubMessage pubMessage = objectMapper.readValue(publishMessage, PubMessage.class);
            //채팅방 구독한 클라이언트-> 메시지 발송
            messagingTemplate.convertAndSend("/sub/chat/" + pubMessage.getRoomId(), pubMessage);
            log.info("publish 후 message: {}", pubMessage.getContents());
        } catch (Exception e) {
            log.error("Exception {}", e.getMessage());
        }
    }
}

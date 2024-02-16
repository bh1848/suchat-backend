package com.USWRandomChat.backend.chat.api;

import com.USWRandomChat.backend.chat.domain.PubMessage;
import com.USWRandomChat.backend.chat.dto.MessageRequest;
import com.USWRandomChat.backend.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic channelTopic;

    //client에서 /pub/chat/message로 요청
    @MessageMapping("/chat/message/{room-id}")
    public void message(@DestinationVariable("room-id") String roomId, MessageRequest messageRequest) {
        //dto message-> redis message
        PubMessage pubMessage =
                new PubMessage(messageRequest.getRoomId(),
                        messageRequest.getSender(),
                        messageRequest.getContents(),
                        LocalDateTime.now());

        //메시지 전송
        redisTemplate.convertAndSend(channelTopic.getTopic(), pubMessage);
        log.info("레디스 서버에 메시지 전송");

        chatService.saveMessage(messageRequest, roomId);
    }
}
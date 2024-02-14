package com.USWRandomChat.backend.chat.api;

import com.USWRandomChat.backend.chat.chatDTO.ChatMessage;
import com.USWRandomChat.backend.chat.domain.ChatRoom;
import com.USWRandomChat.backend.chat.service.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatRepository chatRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic channelTopic;

    //client에서 /pub/chat/message로 요청
    @MessageMapping("/chat/message")
    public void message(ChatMessage message) {
        //메시지 발송 시 /pub/chat/message
        //메시지 수신 시 /sub/chat/room/방 ID
        /*
        닉네임 설정 모호함
        message.setSender("user_1");
        */
        message.setMessage(message.getSender() + "님이 입장하였습니다.");
        redisTemplate.convertAndSend(channelTopic.getTopic(), message);
    }

    //채팅방 생성
    @PostMapping("/chat/room")
    @ResponseBody
    public ChatRoom createRoom(@RequestParam String name) {
        return chatRepository.createChatRoom(name);
    }

    //채팅방 조회
    @GetMapping("/chat/room/{roomId}")
    @ResponseBody
    public ChatRoom roomInfo(@PathVariable String roomId) {
        return chatRepository.findRoomById(roomId);
    }
}
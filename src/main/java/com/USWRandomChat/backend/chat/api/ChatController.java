package com.USWRandomChat.backend.chat.api;

import com.USWRandomChat.backend.chat.chatDTO.ChatMessage;
import com.USWRandomChat.backend.chat.domain.ChatRoom;
import com.USWRandomChat.backend.chat.repository.ChatRepository;
import com.USWRandomChat.backend.chat.service.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final RedisPublisher redisPublisher;
    private final ChatRepository chatRepository;

    //client에서 /pub/chat/message로 요청
    @MessageMapping("/chat/message")
    public void message(ChatMessage message) {
        //메시지 발송 시 /pub/chat/message
        //메시지 수신 시 /sub/chat/room/방 ID
        if (ChatMessage.MessageType.JOIN.equals(message.getType())) {
            chatRepository.enterChatRoom(message.getRoomId());
            message.setMessage(message.getSender() + "님이 입장하였습니다.");
        }
        //webSocket에 발행된 메시지를 redis로 발행한다.(publish)
        redisPublisher.publish(chatRepository.getTopic(message.getRoomId()), message);
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

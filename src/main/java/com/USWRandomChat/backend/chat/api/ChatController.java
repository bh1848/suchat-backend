package com.USWRandomChat.backend.chat.api;

import com.USWRandomChat.backend.chat.chatDTO.ChatMessage;
import com.USWRandomChat.backend.chat.chatDTO.ChatRoom;
import com.USWRandomChat.backend.chat.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatRepository chatRepository;

    //client에서 /pub/chat/message로 요청
    @MessageMapping("/chat/message")
    public void message(ChatMessage message) {
        //메시지 발송 시 /pub/chat/message
        //메시지 수신 시 /sub/chat/room/방 ID
        if (ChatMessage.MessageType.JOIN.equals(message.getType())) {
            message.setMessage(message.getSender() + "님이 입장하였습니다.");
        }
        messagingTemplate.convertAndSend("/sub/chat/room" + message.getRoomId(), message);
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

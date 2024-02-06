package com.USWRandomChat.backend.chat.handler;

import com.USWRandomChat.backend.chat.chatDTO.ChatMessage;
import com.USWRandomChat.backend.chat.service.ChatRepository;
import com.USWRandomChat.backend.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {
    private ChatRepository chatRepository;
    private ChatService chatService;

    //websocket을 통해 들어온 요청이 처리 되기 전 실행
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT == accessor.getCommand()) {//websocket 연결 요청
            log.info("CONNECT");
        }
        else if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
            //header정보에서 구독 destination정보를 얻고, roomId를 추출
            String roomId = chatService.getRoomId(Optional.ofNullable((String) message.getHeaders()
                    .get("simpDestination")).orElse("InvalidRoomId"));

            //채팅방에 들어온 멤버 sessionId를 roomId와 맵핑-> 후에 특정 세션이 어떤 채팅방에 들어갔는지 알기 위해
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            chatRepository.setMemberEnterInfo(sessionId, roomId);

            //입장 메시지 발송-> 채팅방(redis pub)
            String name = Optional.ofNullable((Principal) message.getHeaders()
                    .get("simpMember")).map(Principal::getName).orElse("UnknownMember");
            chatService.sendChatMessage(ChatMessage.builder()
                    .type(ChatMessage.MessageType.ENTER)
                    .roomId(roomId)
                    .sender(name)
                    .build());

            log.info("SUBSCRIBED {}, {}", name, roomId);
        }
        else if (StompCommand.DISCONNECT == accessor.getCommand()) {
            //연결 종료된 멤버 sessionId로 채팅방 id 획득
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            String roomId = chatRepository.getMemberEnterRoomId(sessionId);

            //멤버 퇴장 메시지를 채팅방에 발송(redis pub)
            String name = Optional.ofNullable((Principal) message.getHeaders()
                    .get("simpMember")).map(Principal::getName).orElse("UnknownMember");
            chatService.sendChatMessage(ChatMessage.builder()
                    .type(ChatMessage.MessageType.QUIT)
                    .roomId(roomId)
                    .sender(name)
                    .build());

            chatRepository.removeMemberEnterInfo(sessionId);
            log.info("DISCONNECTED {}, {}", sessionId, roomId);
        }
        return message;
    }

}

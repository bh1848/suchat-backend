package com.USWRandomChat.backend.chat.secure.api;

import com.USWRandomChat.backend.chat.dto.MessageResponse;
import com.USWRandomChat.backend.chat.dto.MultiResponseDto;
import com.USWRandomChat.backend.chat.dto.PageInfo;
import com.USWRandomChat.backend.chat.domain.PubMessage;
import com.USWRandomChat.backend.chat.dto.MessageRequest;
import com.USWRandomChat.backend.chat.mapper.ChatMapper;
import com.USWRandomChat.backend.chat.secure.service.ChatSecureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatSecureController {

    private final ChatSecureService chatSecureService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic channelTopic;
    private final ChatMapper mapper;

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

        chatSecureService.saveMessage(messageRequest, roomId);
    }

    @GetMapping("/chat/message/{room-id}")
    public ResponseEntity getMessages(@Positive @PathVariable("room-id") String roomId,
                                      @Positive @RequestParam(defaultValue = "1") int page,
                                      @Positive @RequestParam(defaultValue = "10") int size){

        //해당 채팅방의 메세지 가져오기
        Page<MessageRequest> messages = chatSecureService.findMessages(roomId, page, size);
        PageInfo pageInfo = new PageInfo(page, size, (int)messages.getTotalElements(), messages.getTotalPages());

        List<MessageRequest> messageList = messages.getContent();
        List<MessageResponse> messageResponses = mapper.messagesToMessageResponseDtos(messageList);

        return new ResponseEntity<>(new MultiResponseDto<>(messageResponses, pageInfo), HttpStatus.OK);
    }
}
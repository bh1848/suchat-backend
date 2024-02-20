package com.USWRandomChat.backend.chat.api;

import com.USWRandomChat.backend.chat.chatDTO.MultiResponseDto;
import com.USWRandomChat.backend.chat.chatDTO.PageInfo;
import com.USWRandomChat.backend.chat.dto.MessageRequest;
import com.USWRandomChat.backend.chat.repository.MessageRepository;
import com.USWRandomChat.backend.chat.service.ChatService;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatService chatService;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ProfileRepository profileRepository;


    @GetMapping("/chats/messages/{room-id}")
    public ResponseEntity getMessages(@Positive @PathVariable("room-id") long roomId,
                                      @Positive @RequestParam(defaultValue = "1") int page,
                                      @Positive @RequestParam(defaultValue = "10") int size){

        //해당 채팅방의 메세지 가져오기
        Page<MessageRequest> messages = chatService.findMessages(roomId, page, size);
        PageInfo pageInfo = new PageInfo(page, size, (int)messages.getTotalElements(), messages.getTotalPages());

        List<MessageRequest> messageList = messages.getContent();

        return new ResponseEntity<>(new MultiResponseDto<>(messageList, pageInfo), HttpStatus.OK);
    }


}

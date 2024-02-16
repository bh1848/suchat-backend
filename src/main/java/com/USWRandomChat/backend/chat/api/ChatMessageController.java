package com.USWRandomChat.backend.chat.api;

import com.USWRandomChat.backend.chat.chatDTO.ChatMessage;
import com.USWRandomChat.backend.chat.chatDTO.MultiResponseDto;
import com.USWRandomChat.backend.chat.chatDTO.PageInfo;
import com.USWRandomChat.backend.chat.domain.Message;
import com.USWRandomChat.backend.chat.repository.MessageRepository;
import com.USWRandomChat.backend.chat.service.ChatService;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.dto.ProfileRequest;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import com.USWRandomChat.backend.security.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
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
        Page<ChatMessage> messages = chatService.findMessages(roomId, page, size);
        PageInfo pageInfo = new PageInfo(page, size, (int)messages.getTotalElements(), messages.getTotalPages());

        List<ChatMessage> messageList = messages.getContent();

        return new ResponseEntity<>(new MultiResponseDto<>(messageList, pageInfo), HttpStatus.OK);
    }


}

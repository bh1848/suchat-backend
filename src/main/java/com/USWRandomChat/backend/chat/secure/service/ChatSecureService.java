package com.USWRandomChat.backend.chat.secure.service;

import com.USWRandomChat.backend.chat.domain.Message;
import com.USWRandomChat.backend.chat.dto.MessageRequest;
import com.USWRandomChat.backend.chat.repository.MessageRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSecureService {

    private final ProfileRepository profileRepository;
    private final MessageRepository messageRepository;

    //채팅방 찾기
    public Profile findRoom(String roomId) {
        Profile profile = findExistRoom(roomId);

        return profile;
    }

    // 채팅방 존재 검증
    private Profile findExistRoom(String roomId) {
        Optional<Profile> optionalProfile = profileRepository.findByRoomId(roomId);

        return optionalProfile.orElse(null);
    }

    public Page<MessageRequest> findMessages(String roomId, int page, int size) {
        Profile profileRoomId = findRoom(roomId);

        Pageable pageable = PageRequest.of(page-1, size, Sort.by("messageNumber").descending());
        Page<MessageRequest> messages = messageRepository.findByRoomId(pageable, profileRoomId);

        return messages;
    }
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

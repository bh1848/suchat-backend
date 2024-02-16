package com.USWRandomChat.backend.chat.service;

import com.USWRandomChat.backend.chat.chatDTO.ChatMessage;
import com.USWRandomChat.backend.chat.repository.MessageRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChannelTopic channelTopic;
    private final RedisTemplate redisTemplate;
    private final ProfileRepository profileRepository;
    private final MessageRepository messageRepository;

    //header의 destination정보에서 roomId추출
    public String getRoomId(String destination) {
        int lastIndex = destination.lastIndexOf('/');
        if (lastIndex != -1) {
            return destination.substring(lastIndex + 1);
        } else {
            return "";
        }
    }

    //메시지 발송-> 채팅방
    public void sendChatMessage(ChatMessage chatMessage) {
        if (ChatMessage.MessageType.ENTER.equals(chatMessage.getType())) {
            chatMessage.setMessage(chatMessage.getSender() + "님이 방에 입장했습니다.");
            chatMessage.setSender("[알림]");
        } else if (ChatMessage.MessageType.QUIT.equals(chatMessage.getType())) {
            chatMessage.setMessage(chatMessage.getSender() + "님이 방에서 퇴장했습니다.");
            chatMessage.setSender("[알림]");
        }
        redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);
    }


    //채팅방 찾기
    public Profile findRoom(long roomId) {
        Profile profile = findExistRoom(roomId);

        return profile;
    }

    // 채팅방 존재 검증
    private Profile findExistRoom(long roomId) {
        Optional<Profile> optionalProfile = profileRepository.findById(roomId);

        //채팅방이 존재하지 않으면 에러 던지게 수정?
        return optionalProfile.orElse(null);
    }

    public Page<ChatMessage> findMessages(long roomId, int page, int size) {
        Profile profile_roomId = findRoom(roomId);

        Pageable pageable = PageRequest.of(page-1, size, Sort.by("MESSAGE_NUMBER").descending());
        Page<ChatMessage> messages = messageRepository.findByRoomId(pageable, profile_roomId);

        return messages;
    }

}

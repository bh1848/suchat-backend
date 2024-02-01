package com.USWRandomChat.backend.chat.repository;

import com.USWRandomChat.backend.chat.domain.ChatRoom;
import com.USWRandomChat.backend.chat.service.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class ChatRepository {

    //채팅방(Topic)에 발행된 메시지 처리 listener
    private final RedisMessageListenerContainer redisMessageListener;
    //구독 처리 서비스
    private final RedisSubscriber redisSubscriber;

    //Redis
    private static final String CHAT_ROOMS = "CHAT_ROOM";
    private final RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, String, ChatRoom> opHashChatRoom;
    /*
     * 채팅방의 대화 메시지를 발행하기 위한 redis topic 정보.
     * 서버별로 채팅방에 맞는 topic 정보를 Map에 넣어 roomId로 찾을 수 있도록한다.
     * */
    private Map<String, ChannelTopic> topics;

    @PostConstruct
    private void init() {
        opHashChatRoom = redisTemplate.opsForHash();
        topics = new HashMap<>();
    }

    public List<ChatRoom> findAllRoom() {
        return opHashChatRoom.values(CHAT_ROOMS);
    }

    public ChatRoom findRoomById(String id) {
        return opHashChatRoom.get(CHAT_ROOMS, id);
    }

    /*
     * 채팅방 생성: 서버간 채팅방 공유<- redis hash에 저장
     * */
    public ChatRoom createChatRoom(String name) {
        ChatRoom chatRoom = ChatRoom.create(name);
        opHashChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);
        return chatRoom;
    }

    /*
     * 채팅방 입장: redis에 topic을 만들고 pub/sub 통신<- 리스너를 설정
     * */
    public void enterChatRoom(String roomId) {
        ChannelTopic topic = topics.get(roomId);
        if (topic == null) {
            topic = new ChannelTopic(roomId);
            redisMessageListener.addMessageListener(redisSubscriber, topic);
            topics.put(roomId, topic);
        }
    }

    public ChannelTopic getTopic(String roomId) {
        return topics.get(roomId);
    }
}

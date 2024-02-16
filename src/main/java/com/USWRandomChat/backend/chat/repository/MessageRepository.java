package com.USWRandomChat.backend.chat.repository;

import com.USWRandomChat.backend.chat.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findLatestMessageByRoomId(String roomId);
    Optional<Message> findTopByRoomIdOrderByMessageNumberDesc(String roomId);
}

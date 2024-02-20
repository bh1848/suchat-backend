package com.USWRandomChat.backend.chat.repository;

import com.USWRandomChat.backend.chat.domain.Message;
import com.USWRandomChat.backend.chat.dto.MessageRequest;
import com.USWRandomChat.backend.profile.domain.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findLatestMessageByRoomId(String roomId);
    Optional<Message> findTopByRoomIdOrderByMessageNumberDesc(String roomId);
    Page<MessageRequest> findByRoomId(Pageable pageable, Profile profile);

}

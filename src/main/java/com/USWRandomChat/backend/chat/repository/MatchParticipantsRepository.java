package com.USWRandomChat.backend.chat.repository;

import com.USWRandomChat.backend.chat.domain.MatchParticipants;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchParticipantsRepository extends JpaRepository<MatchParticipants, Long> {

}

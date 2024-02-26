package com.USWRandomChat.backend.profile.repository;

import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.profile.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    // 추가적인 쿼리 메서드가 필요하다면 여기에 작성
    Optional<Profile> findByRoomId(String id);

    Profile findByNickname(String nickname);

    Profile findByMember(Member member);
}

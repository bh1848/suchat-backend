package com.USWRandomChat.backend.profile.repository;

import com.USWRandomChat.backend.profile.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    //닉네임으로 찾기
    Optional<Profile> findByNickname(String nickname);
}

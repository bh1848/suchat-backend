package com.USWRandomChat.backend.repository;

import com.USWRandomChat.backend.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    //memberId로 찾기
    Optional<Member> findByMemberId(String memberId);
    //email로 찾기
    Optional<Member> findByEmail(String email);

    //닉네임으로 찾기
    Optional<Member> findByNickname(String nickname);
}

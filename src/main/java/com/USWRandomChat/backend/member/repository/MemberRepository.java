package com.USWRandomChat.backend.member.repository;

import com.USWRandomChat.backend.member.domain.Member;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    //memberId로 찾기
    Optional<Member> findByMemberId(String memberId);

    //email로 찾기
    Member findByEmail(String email);

    //닉네임으로 찾기
    Optional<Member> findByNickname(String nickname);

    //id로 삭제하기
    void deleteById(Long id);

    // memberId와 email로 회원 찾기
    Optional<Member> findByMemberIdAndEmail(String memberId, String email);
}
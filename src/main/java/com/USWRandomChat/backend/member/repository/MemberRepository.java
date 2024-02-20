package com.USWRandomChat.backend.member.repository;

import com.USWRandomChat.backend.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    //account로 찾기
    Member findByAccount(String account);

    //email로 찾기
    Member findByEmail(String email);
}
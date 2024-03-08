package com.USWRandomChat.backend.member.repository;


import com.USWRandomChat.backend.member.domain.MemberTemp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTempRepository extends JpaRepository<MemberTemp, Long> {
    //email로 찾기
    MemberTemp findByEmail(String email);
}

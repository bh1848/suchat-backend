package com.USWRandomChat.backend.member.service;

import com.USWRandomChat.backend.member.repository.MemberTempRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberTempService {
    private final MemberTempRepository memberTempRepository;

    //해당 토큰 유저 삭제
    public void deleteFromId(Long id) {
        memberTempRepository.deleteById(id);
    }

}

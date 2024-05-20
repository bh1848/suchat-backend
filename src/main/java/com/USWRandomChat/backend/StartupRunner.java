package com.USWRandomChat.backend;

import com.USWRandomChat.backend.email.service.EmailAuthSchedulerService;
import com.USWRandomChat.backend.member.domain.MemberTemp;
import com.USWRandomChat.backend.member.dto.SignUpRequest;
import com.USWRandomChat.backend.member.open.service.MemberOpenService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StartupRunner implements CommandLineRunner {

    private final EmailAuthSchedulerService schedulerService;
    private final MemberOpenService memberOpenService;

    @Override
    public void run(String... args) {
        schedulerService.removeMember();
    }
}
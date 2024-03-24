package com.USWRandomChat.backend;

import com.USWRandomChat.backend.auth.service.AuthService;
import com.USWRandomChat.backend.emailAuth.service.EmailAuthSchedulerService;
import com.USWRandomChat.backend.member.domain.MemberTemp;
import com.USWRandomChat.backend.member.memberDTO.SignUpRequest;
import com.USWRandomChat.backend.member.service.MemberService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StartupRunner implements CommandLineRunner {

    private final EmailAuthSchedulerService schedulerService;
    private final AuthService authService;

    @Override
    public void run(String... args) {
        schedulerService.removeMember();

        //회원가입 정보 주입
        SignUpRequest initialMemberRequest = new SignUpRequest();
        initialMemberRequest.setAccount("account");
        initialMemberRequest.setPassword("password");
        initialMemberRequest.setEmail("suwon");
        initialMemberRequest.setNickname("nickname");
        initialMemberRequest.setIsEmailVerified(true);

        MemberTemp initialMemberTemp = authService.signUpMemberTemp(initialMemberRequest);
        authService.signUpMember(initialMemberTemp);

        //두번 쨰 회원가입 정보 주입
        SignUpRequest secondinitialMemberRequest = new SignUpRequest();
        secondinitialMemberRequest.setAccount("admin");
        secondinitialMemberRequest.setPassword("1234");
        secondinitialMemberRequest.setEmail("suwonsuwon");
        secondinitialMemberRequest.setNickname("nick");
        secondinitialMemberRequest.setIsEmailVerified(true);

        MemberTemp secondinitialMemberTemp = authService.signUpMemberTemp(secondinitialMemberRequest);
        authService.signUpMember(secondinitialMemberTemp);
    }
}
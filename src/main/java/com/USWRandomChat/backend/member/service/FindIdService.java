package com.USWRandomChat.backend.member.service;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.MailException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static com.USWRandomChat.backend.global.exception.ExceptionType.EMAIL_NOT_AUTHED;
import static com.USWRandomChat.backend.global.exception.ExceptionType.SEND_MAIL_FAILED;

@Service
@RequiredArgsConstructor
@Slf4j
public class FindIdService {

    private final MemberRepository memberRepository;
    private final JavaMailSender javaMailSender;
    public boolean findById(String email) throws MessagingException {
        Member findmember = memberRepository.findByEmail(email);

        // 이메일에 해당하는 회원이 없을 때 예외 발생
        if (findmember == null){
            throw new AccountException(ExceptionType.USER_NOT_EXISTS);
        }

        try {
            //MimeMessage  생성
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);
            helper.setTo(findmember.getEmail() + "@suwon.ac.kr"); // 수신자 이메일 설정
            helper.setSubject("수원대학교 SWCHAT의 아이디 찾기 위한 메일입니다."); // 이메일 제목
            helper.setText("회원님의 아이디는 " + findmember.getAccount() + " 입니다."); // 이메일 내용 설정

            javaMailSender.send(mimeMessage);
            return true;
        } catch (MessagingException e){
            e.printStackTrace();
            throw new MailException(SEND_MAIL_FAILED);
        }
    }

}
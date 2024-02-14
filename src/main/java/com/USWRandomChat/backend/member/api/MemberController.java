package com.USWRandomChat.backend.member.api;

import com.USWRandomChat.backend.emailAuth.service.EmailService;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.exception.CheckDuplicateNicknameException;
import com.USWRandomChat.backend.member.exception.NicknameChangeNotAllowedException;
import com.USWRandomChat.backend.member.memberDTO.MemberDTO;
import com.USWRandomChat.backend.member.memberDTO.SignInRequest;
import com.USWRandomChat.backend.member.memberDTO.SignInResponse;
import com.USWRandomChat.backend.member.memberDTO.SignUpRequest;
import com.USWRandomChat.backend.member.service.MemberService;
import com.USWRandomChat.backend.response.ListResponse;
import com.USWRandomChat.backend.response.ResponseService;
import com.USWRandomChat.backend.security.jwt.dto.TokenDto;
import com.USWRandomChat.backend.security.jwt.service.JwtService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;


@Slf4j
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final ResponseService responseService;
    private final EmailService emailService;
    private final JwtService jwtService;

    //회원가입
    @PostMapping(value = "/sign-up")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest request) throws MessagingException {
        Member findMember = memberService.signUp(request);
        return new ResponseEntity<>(new SignUpResponse(emailService.createEmailToken(findMember))
                , HttpStatus.OK);
    }

    //로그인
    @PostMapping(value = "/sign-in")
    public ResponseEntity<SignInResponse> signIn(@RequestBody SignInRequest request) {
        return new ResponseEntity<>(memberService.signIn(request), HttpStatus.OK);
    }

    //로그아웃
    @PostMapping("/sign-out")
    public ResponseEntity<String> signOut(@RequestParam String memberId) {
        try {
            jwtService.signOut(memberId);
            return new ResponseEntity<>("로그아웃 성공", HttpStatus.OK);
        } catch (Exception e) {
            log.error("로그아웃 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("로그아웃 실패");
        }
    }

    // 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestParam String memberId) {
        try {
            memberService.withdraw(memberId);
            return new ResponseEntity<>("회원 탈퇴 성공", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            log.error("회원 탈퇴 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("존재하지 않는 회원입니다.");
        } catch (Exception e) {
            log.error("회원 탈퇴 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원 탈퇴 실패");
        }
    }

    //이메일 인증 확인
    @GetMapping("/confirm-email")
    public ResponseEntity<Boolean> viewConfirmEmail(@Valid @RequestParam String uuid) {
        return new ResponseEntity<>(emailService.verifyEmail(uuid), HttpStatus.OK);
    }

    //이메일 재인증
    @PostMapping("/reconfirm-email")
    public ResponseEntity<String> reconfirmEmail(@RequestParam String uuid) throws MessagingException {
        return new ResponseEntity<>(emailService.recreateEmailToken(uuid), HttpStatus.OK);
    }


    //user인증 확인
//    @GetMapping(value = "/user/get")
//    public ResponseEntity<SignInResponse> getUser(@RequestParam String account) throws Exception {
//        return new ResponseEntity<>(memberService.getMember(account), HttpStatus.OK);
//    }


    //전체 조회
    @GetMapping("/members")
    public ListResponse<Member> findAll() {
        return responseService.getListResponse(memberService.findAll());
    }

//    // account 중복 체크
//    @PostMapping("/check-duplicate-id")
//    public boolean idCheck(@RequestBody MemberDTO request) {
//        boolean checkResult = memberService.validateDuplicateAccount(request);
//        if (checkResult == false) {
//            //사용가능한 ID
//            return true;
//        } else {
//            //중복
//            return false;
//        }
//    }

    //닉네임 중복 확인
    @PostMapping("/check-duplicate-nickname")
    public ResponseEntity<String> checkDuplicateNickname(@RequestParam String account, @RequestBody MemberDTO memberDTO) {
        try {
            memberService.checkDuplicateNickname(account, memberDTO);
            return new ResponseEntity<>("사용 가능한 닉네임입니다.", HttpStatus.OK);
        } catch (CheckDuplicateNicknameException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NicknameChangeNotAllowedException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    //자동 로그인 로직: 엑세스 토큰, 리프레시 토큰 재발급
    @PostMapping("/auto-sign-in")
    public ResponseEntity<TokenDto> refresh(@RequestBody TokenDto token) throws Exception {
        return new ResponseEntity<>(jwtService.refreshAccessToken(token), HttpStatus.OK);
    }

    @Data
    @AllArgsConstructor
    static class SignUpResponse {

        private String uuid;
    }

    //임의의 데이터 넣기
    @PostConstruct
    public void init() throws MessagingException {
        Member findMember = memberService.signUp(new SignUpRequest("admin", "password", "cookie_31", "nick"));
        emailService.createEmailToken(findMember);
    }
}
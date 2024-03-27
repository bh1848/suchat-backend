package com.USWRandomChat.backend.member.open.api;

import com.USWRandomChat.backend.email.service.EmailService;
import com.USWRandomChat.backend.global.response.ApiResponse;
import com.USWRandomChat.backend.global.response.ListResponse;
import com.USWRandomChat.backend.global.response.ResponseService;
import com.USWRandomChat.backend.global.security.jwt.dto.TokenDto;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.domain.MemberTemp;
import com.USWRandomChat.backend.member.dto.*;
import com.USWRandomChat.backend.member.open.service.FindIdService;
import com.USWRandomChat.backend.member.open.service.MemberOpenService;
import com.USWRandomChat.backend.member.open.service.PasswordUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/member/open")
@RequiredArgsConstructor
public class MemberOpenController {

    private final EmailService emailService;
    private final MemberOpenService memberOpenService;
    private final ResponseService responseService;
    private final FindIdService findIdService;

    //member_table에 들어가기 전 임시 데이터 넣기
    @PostMapping(value = "/sign-up")
    public ResponseEntity<ApiResponse> signUp(@RequestBody SignUpRequest request) throws MessagingException {
        MemberTemp findTempMember = memberOpenService.signUpMemberTemp(request);
        return new ResponseEntity<>(new ApiResponse(emailService.createEmailToken(findTempMember))
                , HttpStatus.OK);
    }

    //이메일 인증 확인 후 회원가입
    @GetMapping("/confirm-email")
    public ResponseEntity<Boolean> viewConfirmEmail(@Valid @RequestParam String uuid) {
        MemberTemp memberTemp = emailService.findByUuid(uuid);
        memberOpenService.signUpMember(memberTemp);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    //회원가입 완료
    @GetMapping(value = "/sign-up-finish")
    public ResponseEntity<Boolean> signUpFinish(@RequestParam String uuid) {
        return new ResponseEntity<>(memberOpenService.signUpFinish(uuid), HttpStatus.OK);
    }

    //로그인
    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse> signIn(@RequestBody SignInRequest request, HttpServletResponse response) {
        TokenDto tokenDto = memberOpenService.signIn(request, response);
        return ResponseEntity.ok(new ApiResponse("로그인 되었습니다.", tokenDto));
    }

    //이메일 재인증
    @PostMapping("/reconfirm-email")
    public ResponseEntity<ApiResponse> reconfirmEmail(@Valid @RequestParam String uuid) throws MessagingException {
        emailService.recreateEmailToken(uuid);
        return ResponseEntity.ok(new ApiResponse("이메일 재인증을 해주세요", uuid));
    }

    //회원가입 시의 계정 중복 체크
    @PostMapping("/check-duplicate-account")
    public ResponseEntity<ApiResponse> checkDuplicateAccount(@RequestBody MemberDto request) {
        memberOpenService.checkDuplicateAccount(request);
        return ResponseEntity.ok(new ApiResponse("사용 가능한 계정입니다."));
    }

    //회원가입 시의 이메일 중복 확인
    @PostMapping("/check-duplicate-email")
    public ResponseEntity<ApiResponse> checkDuplicateEmail(@RequestBody MemberDto memberDTO) {
        memberOpenService.checkDuplicateEmail(memberDTO);
        return ResponseEntity.ok(new ApiResponse("사용 가능한 이메일입니다."));
    }

    //회원가입 시의 닉네임 확인
    @PostMapping("/check-duplicate-nickname-signUp")
    public ResponseEntity<ApiResponse> checkDuplicateNicknameSignUp(@RequestBody MemberDto memberDTO) {
        memberOpenService.checkDuplicateNicknameSignUp(memberDTO);
        return ResponseEntity.ok(new ApiResponse("사용 가능한 닉네임입니다."));

    }

    //전체 조회(테스트 용도)
    @GetMapping("/members")
    public ListResponse<Member> findAll() {
        return responseService.getListResponse(memberOpenService.findAll());
    }

    //Id 찾기 로직: 이메일 인증된 회원만
    @PostMapping(value = "/find-Id")
    public ResponseEntity<Boolean> findId(@RequestParam String email) {
        return new ResponseEntity<>(findIdService.findById(email), HttpStatus.OK);
    }
    private final PasswordUpdateService passwordUpdateService;

    //인증번호 생성 및 전송 요청 처리
    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse> sendVerificationCode(@RequestBody SendVerificationCodeRequest request) {
        String uuid = String.valueOf(passwordUpdateService.sendVerificationCode(request.getAccount(), request.getEmail()));
        SendVerificationCodeResponse response = new SendVerificationCodeResponse(uuid);
        return ResponseEntity.ok(new ApiResponse("인증번호가 전송되었습니다.", response));
    }

    //인증번호 확인 요청 처리
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse> verifyCode(@RequestParam String uuid, @RequestParam String verificationCode) {
        boolean isVerified = passwordUpdateService.verifyCode(uuid, verificationCode);
        if (isVerified) {
            return ResponseEntity.ok(new ApiResponse("인증번호가 확인됐습니다."));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("인증번호가 맞지 않습니다."));
        }
    }

    //비밀번호 변경 요청 처리
    @PatchMapping("/update-password")
    public ResponseEntity<ApiResponse> updatePassword(@RequestParam String uuid, @RequestBody UpdatePasswordRequest request) {
        passwordUpdateService.updatePassword(uuid, request.getNewPassword(), request.getConfirmNewPassword());
        return ResponseEntity.ok(new ApiResponse("비밀번호가 변경됐습니다."));
    }
}
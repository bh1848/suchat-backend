package com.USWRandomChat.backend.member.open.api;

import com.USWRandomChat.backend.chat.secure.service.RoomSecureService;
import com.USWRandomChat.backend.email.service.EmailService;
import com.USWRandomChat.backend.global.response.ApiResponse;
import com.USWRandomChat.backend.global.response.ListResponse;
import com.USWRandomChat.backend.global.response.ResponseService;
import com.USWRandomChat.backend.global.security.jwt.dto.TokenDto;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.domain.MemberTemp;
import com.USWRandomChat.backend.member.dto.*;
import com.USWRandomChat.backend.member.open.service.FindAccountService;
import com.USWRandomChat.backend.member.open.service.MemberOpenService;
import com.USWRandomChat.backend.member.open.service.PasswordUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/open/member")
@RequiredArgsConstructor
public class MemberOpenController {

    private final EmailService emailService;
    private final MemberOpenService memberOpenService;
    private final ResponseService responseService;
    private final FindAccountService findAccountService;
    private final PasswordUpdateService passwordUpdateService;
    private final RoomSecureService roomSecureService;

    //이메일 인증 전 임시 데이터 넣기
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse> signUp(@Valid @RequestBody SignUpRequest request) throws MessagingException {
        MemberTemp findTempMember = memberOpenService.signUpMemberTemp(request);
        Map<String, Object> response = emailService.createEmailToken(findTempMember);
        ApiResponse apiResponse = new ApiResponse("임시 회원 가입이 되었습니다.", response);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostConstruct
    public void initializeDummyData() {
        createAndSignUpMember("account", "password", "suwon", "nickname", true, "1234");
        createAndSignUpMember("admin", "1234", "suwonsuwon", "nick", true, "1234");
    }

    private void createAndSignUpMember(String account, String password, String email, String nickname, boolean isEmailVerified, String roomId) {
        SignUpRequest request = new SignUpRequest();
        request.setAccount(account);
        request.setPassword(password);
        request.setEmail(email);
        request.setNickname(nickname);
        request.setIsEmailVerified(isEmailVerified);

        MemberTemp memberTemp = memberOpenService.signUpMemberTemp(request);
        memberOpenService.signUpMember(memberTemp);
        roomSecureService.updateMemberRoomId(memberTemp.getAccount(), roomId);
    }

    //이메일 인증 확인
    @GetMapping("/confirm-email")
    public ResponseEntity<ApiResponse> viewConfirmEmail(@Valid @RequestParam String uuid) {
        MemberTemp memberTemp = emailService.findByUuid(uuid);
        memberOpenService.signUpMember(memberTemp);

        return ResponseEntity.ok(new ApiResponse("인증이 성공되었습니다. 어플 내에서 회원가입 완료를 눌러주세요"));
    }

    //회원가입 완료
    @PostMapping("/sign-up-finish")
    public ResponseEntity<Boolean> signUpFinish(@RequestBody MemberDto memberDTO) {
        return new ResponseEntity<>(memberOpenService.signUpFinish(memberDTO.getAccount()), HttpStatus.OK);
    }

    //이메일 재인증
    @PostMapping("/reconfirm-email")
    public ResponseEntity<ApiResponse> reconfirmEmail(@Valid @RequestParam String uuid) throws MessagingException {
        emailService.recreateEmailToken(uuid);
        return ResponseEntity.ok(new ApiResponse("이메일 재인증을 해주세요", uuid));
    }

    //로그인
    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse> signIn(@RequestBody SignInRequest request, HttpServletResponse response) {
        TokenDto tokenDto = memberOpenService.signIn(request, response);
        return ResponseEntity.ok(new ApiResponse("로그인 되었습니다.", tokenDto));
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

    //Account 찾기 로직: 이메일 인증된 회원만
    @PostMapping(value = "/find-Account")
    public ResponseEntity<Boolean> findAccount(@RequestParam String email) throws MessagingException {
        return new ResponseEntity<>(findAccountService.findByAccount(email), HttpStatus.OK);
    }

    //인증번호 생성 및 전송 요청 처리
    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse> sendVerificationCode(@RequestBody SendVerificationCodeRequest request) {
        String uuid = String.valueOf(passwordUpdateService.sendVerificationCode(request.getAccount(), request.getEmail()));
        // HttpHeaders 객체 생성 및 UUID를 헤더에 추가
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-ID", uuid);
        ApiResponse response = new ApiResponse("인증번호가 전송되었습니다.", uuid);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    //인증번호 확인 요청 처리
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse> verifyCode(@RequestHeader("X-User-ID") String uuid, @RequestBody VerificationCodeRequest request) {
        passwordUpdateService.verifyCode(uuid, request.getVerificationCode());
        return ResponseEntity.ok(new ApiResponse("인증번호가 확인됐습니다."));
    }

    //비밀번호 변경 요청 처리
    @PatchMapping("/update-password")
    public ResponseEntity<ApiResponse> updatePassword(@RequestHeader("X-User-ID") String uuid, @RequestBody UpdatePasswordRequest request) {
        passwordUpdateService.updatePassword(uuid, request.getNewPassword(), request.getConfirmNewPassword());
        return ResponseEntity.ok(new ApiResponse("비밀번호가 변경됐습니다."));
    }
}
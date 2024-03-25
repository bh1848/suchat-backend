package com.USWRandomChat.backend.member.secure.api;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.response.ApiResponse;
import com.USWRandomChat.backend.global.response.ListResponse;
import com.USWRandomChat.backend.global.response.ResponseService;
import com.USWRandomChat.backend.global.security.jwt.service.JwtService;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.dto.MemberDTO;
import com.USWRandomChat.backend.member.open.service.FindIdService;
import com.USWRandomChat.backend.member.secure.service.MemberSecureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/member/secure")
@RequiredArgsConstructor
public class MemberSecureController {
    
    private final MemberSecureService memberSecureService;
    private final ResponseService responseService;
    private final FindIdService findIdService;

    //로그아웃
    @PostMapping("/sign-out")
    public ResponseEntity<ApiResponse> signOut(HttpServletRequest request, HttpServletResponse response){
        try {
            memberSecureService.signOut(request, response);
            return ResponseEntity.ok(new ApiResponse("로그아웃 되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("로그아웃에 실패했습니다."));
        }
    }

    //회원 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse> withdraw(@RequestHeader("Authorization") String accessToken) {
        try {
            memberSecureService.withdraw(accessToken);
            return ResponseEntity.ok(new ApiResponse("회원 탈퇴가 완료됐습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("회원 탈퇴에 실패했습니다."));
        }
    }

    //전체 조회
    @GetMapping("/members")
    public ListResponse<Member> findAll() {
        return responseService.getListResponse(memberSecureService.findAll());
    }

    //회원가입 시의 닉네임 확인
    @PostMapping("/check-duplicate-nickname-signUp")
    public ResponseEntity<ApiResponse> checkDuplicateNicknameSignUp(@RequestBody MemberDTO memberDTO) {
        try {
            memberSecureService.checkDuplicateNicknameSignUp(memberDTO);
            return ResponseEntity.ok(new ApiResponse("사용 가능한 닉네임입니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("이미 사용하고 있는 닉네임입니다."));
        }
    }

    //이미 가입된 사용자의 닉네임 중복 확인
    @PostMapping("/check-nickname")
    public ResponseEntity<ApiResponse> checkDuplicateNickname(@RequestHeader("Authorization") String accessToken, @RequestBody MemberDTO memberDTO) {
        try {
            memberSecureService.checkDuplicateNickname(accessToken, memberDTO);
            return ResponseEntity.ok(new ApiResponse( "닉네임 사용 가능합니다."));
        } catch (AccountException e) {
            if (e.getExceptionType() == ExceptionType.NICKNAME_EXPIRATION_TIME) {
                // 닉네임 변경 30일 제한 예외 처리
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse("닉네임을 변경한 지 30일이 지나지 않았습니다."));
            } else {
                // 닉네임 중복 예외 처리
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse( "이미 사용하고 있는 닉네임입니다."));
            }
        } catch (Exception e) {
            // 기타 예외 처리
            return ResponseEntity.internalServerError().body(new ApiResponse("서버 오류가 발생했습니다."));
        }
    }

    //Id 찾기 로직: 이메일 인증된 회원만
    @PostMapping(value = "/find-Id")
    public ResponseEntity<Boolean> findId(@RequestParam String email) {
        return new ResponseEntity<>(findIdService.findById(email), HttpStatus.OK);
    }
}
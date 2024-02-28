package com.USWRandomChat.backend.member.verification.api;

import com.USWRandomChat.backend.member.verification.dto.SendVerificationCodeRequest;
import com.USWRandomChat.backend.member.verification.dto.UpdatePasswordRequest;
import com.USWRandomChat.backend.member.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    //인증번호 생성 및 전송 요청 처리
    @PostMapping("/send-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody SendVerificationCodeRequest request) {
        try {
            String uuid = verificationService.sendVerificationCode(request.getAccount(), request.getEmail());
            return ResponseEntity.ok().body("인증번호가 전송되었습니다. UUID: " + uuid);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("인증번호 전송 실패: " + e.getMessage());
        }
    }

    //인증번호 확인 요청 처리
    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestParam String uuid, String verificationCode) {
        try {
            boolean isVerified = verificationService.verifyCode(uuid, verificationCode);
            return isVerified ? ResponseEntity.ok().body("인증번호 확인 성공")
                    : ResponseEntity.badRequest().body("인증번호 확인 실패");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("인증번호 확인 에러: " + e.getMessage());
        }
    }

    //비밀번호 변경 요청 처리
    @PatchMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestParam String uuid ,@RequestBody UpdatePasswordRequest request) {
        try {
            verificationService.updatePassword(uuid, request.getNewPassword(), request.getConfirmNewPassword());
            return ResponseEntity.ok().body("비밀번호 변경 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("비밀번호 변경 실패: " + e.getMessage());
        }
    }
}
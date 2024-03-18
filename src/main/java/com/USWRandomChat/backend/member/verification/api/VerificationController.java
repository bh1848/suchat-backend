package com.USWRandomChat.backend.member.verification.api;

import com.USWRandomChat.backend.global.exception.ApiResponse;
import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.CodeException;
import com.USWRandomChat.backend.member.verification.dto.SendVerificationCodeRequest;
import com.USWRandomChat.backend.member.verification.dto.SendVerificationCodeResponse;
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
    public ResponseEntity<SendVerificationCodeResponse> sendVerificationCode(@RequestBody SendVerificationCodeRequest request) {
        try {
            String uuid = String.valueOf(verificationService.sendVerificationCode(request.getAccount(), request.getEmail()));
            SendVerificationCodeResponse response = new SendVerificationCodeResponse(uuid);
            return ResponseEntity.ok(response); // JSON 형태로 UUID 포함하여 반환
        } catch (Exception e) {
            throw new CodeException(ExceptionType.UUID_NOT_FOUND);
        }
    }

    //인증번호 확인 요청 처리
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse> verifyCode(@RequestParam String uuid, @RequestParam String verificationCode) {
        boolean isVerified = verificationService.verifyCode(uuid, verificationCode);
        if (isVerified) {
            return ResponseEntity.ok(new ApiResponse("인증번호 확인 성공"));
        } else {
            throw new CodeException(ExceptionType.CODE_ERROR);
        }
    }

    //비밀번호 변경 요청 처리
    @PatchMapping("/update-password")
    public ResponseEntity<ApiResponse> updatePassword(@RequestParam String uuid, @RequestBody UpdatePasswordRequest request) {
        try {
            verificationService.updatePassword(uuid, request.getNewPassword(), request.getConfirmNewPassword());
            return ResponseEntity.ok(new ApiResponse("비밀번호 변경 성공"));
        } catch (Exception e) {
            throw new AccountException(ExceptionType.PASSWORD_ERROR);
        }
    }
}
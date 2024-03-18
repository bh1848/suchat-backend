package com.USWRandomChat.backend.member.verification.api;

import com.USWRandomChat.backend.global.response.ApiResponse;
import com.USWRandomChat.backend.member.verification.dto.SendVerificationCodeRequest;
import com.USWRandomChat.backend.member.verification.dto.SendVerificationCodeResponse;
import com.USWRandomChat.backend.member.verification.dto.UpdatePasswordRequest;
import com.USWRandomChat.backend.member.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    //인증번호 생성 및 전송 요청 처리
    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse> sendVerificationCode(@RequestBody SendVerificationCodeRequest request) {
        try {
            String uuid = String.valueOf(verificationService.sendVerificationCode(request.getAccount(), request.getEmail()));
            SendVerificationCodeResponse response = new SendVerificationCodeResponse(uuid);
            return ResponseEntity.ok(new ApiResponse("인증번호가 전송되었습니다.", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse("인증번호 전송 중 오류가 발생했습니다."));
        }
    }

    //인증번호 확인 요청 처리
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse> verifyCode(@RequestParam String uuid, @RequestParam String verificationCode) {
        boolean isVerified = verificationService.verifyCode(uuid, verificationCode);
        if (isVerified) {
            return ResponseEntity.ok(new ApiResponse("인증번호가 확인됐습니다."));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("인증번호가 맞지 않습니다."));
        }
    }

    //비밀번호 변경 요청 처리
    @PatchMapping("/update-password")
    public ResponseEntity<ApiResponse> updatePassword(@RequestParam String uuid, @RequestBody UpdatePasswordRequest request) {
        try {
            verificationService.updatePassword(uuid, request.getNewPassword(), request.getConfirmNewPassword());
            return ResponseEntity.ok(new ApiResponse("비밀번호가 변경됐습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("비밀번호 변경에 실패했습니다."));
        }
    }
}
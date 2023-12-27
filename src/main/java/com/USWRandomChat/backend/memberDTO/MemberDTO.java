package com.USWRandomChat.backend.memberDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberDTO {

    //회원가입 필수 4가지
    //요청, 응답 합침(회원 가입은 변동사항 적음)
    private String memberId;
    private String password;
    private String email;
    private String nickname;
}

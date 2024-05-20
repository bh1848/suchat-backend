package com.USWRandomChat.backend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ExceptionType {
//    100-199: 인증 및 권한
//    200-299: 사용자 관리
//    300-399: 데이터 관리
//    400-499: 토큰 및 인증
//    500-599: 메일 및 통신

    /**
     * SERVER ERROR
     */
    SERVER_ERROR("SER-501", "서버 오류 입니다. 관리자에게 문의해주세요", INTERNAL_SERVER_ERROR),


    /**
     * Domain: EmailToken
     */

    EMAILTOKEN_IS_EXPIRED("EMAILTOKEN001", "토큰이 만료되었습니다. 다시 이메일인증 해주세요", BAD_REQUEST),
    EMAIL_NOT_VERIFIED("EMAILTOKEN002", "이메일 인증되지 않은 회원입니다.", FORBIDDEN),
    Email_Token_Not_Found("EMAILTOKEN003", "해당 토큰이 존재하지 않습니다.", BAD_REQUEST),
    EMAILTOKEN_AND_MEMBERTEMP_Not_Found("EMAILTOKEN004", "해당 토큰과 일치하는 회원이 존재하지 않습니다.", BAD_REQUEST),
    EMAIL_RECERTIFICATION("EMAILTOKEN005","이메일 재인증을 해주세요.",BAD_REQUEST),


    /**
     * Domain: Member
     */
    USER_NOT_EXISTS("USR-MEM-201", "사용자가 존재하지 않습니다.", BAD_REQUEST),
    ACCOUNT_OVERLAP("USR-MEM-202", "계정이 중복됩니다.", INTERNAL_SERVER_ERROR),
    EMAIL_OVERLAP("USR-MEM-203", "이메일이 중복됩니다.", INTERNAL_SERVER_ERROR),
    PASSWORD_ERROR("USR-MEM-204", "비밀번호를 확인해주세요", BAD_REQUEST),
    SIGN_IN_REQUIRED("USR-MEM-205", "로그인이 필요합니다.", FORBIDDEN),
    BAD_CREDENTIALS("USR-MEM-206", "잘못된 계정 정보입니다.", FORBIDDEN),
    Email_Not_Provided("USR-MEM-207", "이메일이 입력되지 않았습니다.", BAD_REQUEST),
    Account_Not_Provided("USR-MEM-208", "아이디가 입력되지 않았습니다.", BAD_REQUEST),
    Nickname_Not_Provided("USR-MEM-209", "닉네임이 입력되지 않았습니다.", BAD_REQUEST),
    WITH_DRAW_FAIL("USR-MEM-210","회원 탈퇴에 실패했습니다.",BAD_REQUEST),
    SIGN_OUT_FAIL("USR-MEM-211", "로그아웃에 실패했습니다", BAD_REQUEST),
    USER_NOT_AUTHENTICATION("USR-MEM-212", "인증이 필요합니다.", BAD_REQUEST),

    /**
     * Domain : Profile
     */
    PROFILE_NOT_EXISTS("USR-PFL-201", "프로필이 존재하지 않습니다.", BAD_REQUEST),
    NICKNAME_ERROR("USR-PFL-202", "닉네임을 확인해주세요.",BAD_REQUEST),
    NICKNAME_OVERLAP("USR-PFL-203", "닉네임이 중복됩니다.", INTERNAL_SERVER_ERROR),
    NICKNAME_EXPIRATION_TIME("USR-PFL-204", "닉네임 변경 후 30일이 지나야 변경이 가능합니다.", BAD_REQUEST),
    PROFILE_GET_FAIL("USR-PFL-205", "프로필 조회에 실패했습니다.", BAD_REQUEST),
    PROFILE_UPDATE_FAIL("USR-PFL-206", "프로필 업데이트에 실패했습니다.",BAD_REQUEST),


    /**
     * Domain : Token
     */
    ACCESS_TOKEN_EXPIRED("TOK-AUTH-401", "엑세스 토큰 만료", UNAUTHORIZED),
    INVALID_ACCESS_TOKEN("TOK-AUTH-402", "잘못된 엑세스 토큰", BAD_REQUEST),
    REFRESH_TOKEN_EXPIRED("TOK-AUTH-403", "리프레시 토큰 만료", FORBIDDEN),
    INVALID_REFRESH_TOKEN("TOK-AUTH-404", "잘못된 리프레시 토큰", BAD_REQUEST),

    /**
     * Domain : VerificationCode
     */
    CODE_ERROR("VC-101", "인증번호를 확인해주세요", BAD_REQUEST),
    UUID_NOT_FOUND("VC-102","회원의 uuid를 찾을 수 없습니다.", BAD_REQUEST),
    VERIFICATION_NOT_COMPLETED("VC-103", "인증번호가 확인이 되지 않은 사용자 입니다.", BAD_REQUEST),

    /**
     * Domain : Chat
     */
    ROOM_ID_UPDATE_ERROR("CHT-301", "RoomId 업데이트 실패", BAD_REQUEST),
    MEMBER_COUNT_ERROR("CHT-302","인원 확인이 실패했습니다.",BAD_REQUEST),
    MESSAGE_DELETE_ERROR("CHT-303","메세지가 삭제되지 않았습니다.",BAD_REQUEST),
    CHAT_ERROR("CHT-304", "채팅방 오류입니다.", BAD_REQUEST),
    NOT_ENOUGH_MEMBER("CHT-305", "매칭할 회원 수가 부족합니다.", BAD_REQUEST),

    /**
     * Domain:ConfirmationToken
     */
    EMAIL_NOT_AUTHED("EML-AUTH-501", "이메일 인증을 받지 않은 사용자 입니다.", UNAUTHORIZED),

    /**
     * 공통
     */
    SEND_MAIL_FAILED("EML-501", "메일 전송에 실패했습니다.", INTERNAL_SERVER_ERROR);


    private final String code;
    private final String message;
    private final HttpStatus status;
}
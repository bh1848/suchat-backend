package com.USWRandomChat.backend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ExceptionType {

    /**
     * SERVER ERROR
     */
    SERVER_ERROR("SERVER001", "서버 오류 입니다. 관리자에게 문의해주세요", INTERNAL_SERVER_ERROR),


    /**
     * Domain: EmailToken
     */
    EMAILTOKEN_IS_EXPIRED("EMAILTOKEN001", "토큰이 만료되었습니다. 다시 이메일인증 해주세요", BAD_REQUEST),
    EMAIL_NOT_VERIFIED("EMAILTOKEN002", "이메일 인증되지 않은 회원입니다.", FORBIDDEN),
    Email_Token_Not_Found("EMAILTOKEN003", "해당 토큰이 존재하지 않습니다.", BAD_REQUEST),

    /**
     * Domain: Member
     */
    USER_NOT_EXISTS("USER001", "사용자가 존재하지 않습니다.", BAD_REQUEST),
    ID_OVERLAP("USER002", "아이디가 중복됩니다.", BAD_REQUEST),
    EMAIL_OVERLAP("USER002", "이메일이 중복됩니다.", BAD_REQUEST),
    PASSWORD_ERROR("USER003", "비밀번호를 확인해주세요", BAD_REQUEST),
    LOGIN_REQUIRED("USER007", "로그인이 필요합니다.", FORBIDDEN),
    BAD_CREDENTIALS("BAD_CREDENTIALS", "잘못된 계정 정보입니다.", FORBIDDEN),
    Email_Not_Provided("USER008", "이메일이 입력되지 않았습니다.", BAD_REQUEST),
    Account_Not_Provided("USER009", "아이디가 입력되지 않았습니다.", BAD_REQUEST),
    Nickname_Not_Provided("USER010", "닉네임이 입력되지 않았습니다.", BAD_REQUEST),


    /**
     * Domain : Profile
     */
    PROFILE_NOT_EXISTS("PROFILE001", "프로필이 존재하지 않습니다.", BAD_REQUEST),
    NICKNAME_ERROR("PROFILE002", "닉네임을 확인해주세요.",BAD_REQUEST),
    NICKNAME_OVERLAP("PROFILE002", "닉네임이 중복됩니다.", BAD_REQUEST),
    NICKNAME_EXPIRATION_TIME("PROFILE002", "닉네임 변경 후 30일이 지나야 변경이 가능합니다.", BAD_REQUEST),

    /**
     * Domain : Token
     */
    ACCESS_TOKEN_REQUIRED("TOKEN001", "엑세스 토큰이 필요합니다", BAD_REQUEST),
    INVALID_ACCESS_TOKEN("TOKEN002", "잘못된 엑세스 토큰입니다.", BAD_REQUEST),
    REFRESH_TOKEN_EXPIRED("TOKEN003", "리프레시 토큰 만료", BAD_REQUEST),
    INVALID_REFRESH_TOKEN("TOKEN004", "잘못된 리프레시 토큰", BAD_REQUEST),

    /**
     * Domain : VerificationCode
     */
    CODE_ERROR("CODE001", "인증번호를 확인해주세요", BAD_REQUEST),
    UUID_NOT_FOUND("CODE002","회원의 uuid를 찾을 수 없습니다.", BAD_REQUEST),
    VERIFICATION_NOT_COMPLETED("CODE003", "인증번호가 확인이 되지 않은 사용자 입니다.", BAD_REQUEST),

    /**
     * Domain : Chat
     */
    ROOM_ID_UPDATE_ERROR("CHAT001", "RoomId 업데이트 실패", BAD_REQUEST),
    MEMBER_COUNT_ERROR("CHAT002","인원 확인이 실패했습니다.",BAD_REQUEST),
    MESSAGE_DELETE_ERROR("CHAT003","메세지가 삭제되지 않았습니다.",BAD_REQUEST),

    /**
     * Domain:ConfirmationToken
     */
    EMAIL_NOT_AUTHED("CONFIRMATION_TOKEN001", "이메일 인증을 받지 않은 사용자 입니다.", UNAUTHORIZED),

    /**
     * 공통
     */
    SEND_MAIL_FAILED("MAIL001", "메일 전송에 실패했습니다.", INTERNAL_SERVER_ERROR), //500

    ;

    private final String code;
    private final String message;
    private final HttpStatus status;
}
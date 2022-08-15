package com.peoples.api.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    /* 400 BAD_REQUEST : 잘못된 요청 */
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "파라미터가 유효하지 않습니다."),

    /* 400 BAD_REQUEST : 잘못된 요청 */
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "중복된 이메일 입니다."),

    /* 400 BAD_REQUEST : 잘못된 요청 */
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "패스워드가 일치하지 않습니다."),

    /* 401 UNAUTHORIZED : 인증되지 않은 사용자 */
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다. 다시 로그인 하세요."),

    /* 403 FORBIDDEN : 권한이 없는 사용자 */
    TOKEN_EXPIRED(HttpStatus.FORBIDDEN, "토큰이 만료되었습니다. 재발급 요청하세요."),

    /* 404 NOT_FOUND : Resource를 찾을 수 없음 */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."),

    /* 404 NOT_FOUND : Resource를 찾을 수 없음 */
    RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "검색 결과가 존재하지 않습니다."),

    /* 404 NOT_FOUND : Resource를 찾을 수 없음 */
    IMG_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지파일이 존재하지 않습니다."),

    /* 409 CONFLICT : Resource의 현재 상태와 충돌, 중복된 데이터 문제 */
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "데이터가 이미 존재합니다."),

    /* 500 INTERNAL_SERVER_ERROR : 쿼리 오류 */
    QUERY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "쿼리에 오류가 있습니다."),

    /* 500 INTERNAL_SERVER_ERROR : 서버 오류 */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String message;
}

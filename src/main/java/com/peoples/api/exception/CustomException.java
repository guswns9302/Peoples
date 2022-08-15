package com.peoples.api.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException{
    private static final long serialVersionUID = 5742209104257968540L;

    private final ErrorCode errorCode;
}

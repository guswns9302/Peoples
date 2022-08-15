package com.peoples.api.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSignUpRequest {
    private String userId;
    private String password;
    private String nickname;
    private String img;
}

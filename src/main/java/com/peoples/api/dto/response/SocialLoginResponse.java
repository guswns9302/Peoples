package com.peoples.api.dto.response;

import com.peoples.api.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class SocialLoginResponse {

    private User user;
    private String accessToken;
    private String refreshToken;
    private boolean firstLogin;

    public static SocialLoginResponse from (User user, String accessToken, String refreshToken, boolean firstLogin){
        return SocialLoginResponse.builder()
                .user(user)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .firstLogin(firstLogin)
                .build();
    }
}

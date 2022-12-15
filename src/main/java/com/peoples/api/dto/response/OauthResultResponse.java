package com.peoples.api.dto.response;

import com.peoples.api.domain.User;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Getter
@Builder
public class OauthResultResponse {
    private String userId;
    private String nickname;
    private String img;
    private boolean emailAuthentication;
    private boolean userStats;
    private boolean userBlock;
    private boolean userPause;
    private boolean pushStart;
    private boolean pushImminent;
    private boolean pushDayAgo;
    private boolean firstLogin;

    public static OauthResultResponse of(User user, boolean isFirstLogin){
        String fileName = "fileName";
        OauthResultResponse userResponse = OauthResultResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .img(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/downloadIMG").queryParam(fileName, user.getImg()).toUriString())
                .emailAuthentication(user.isEmailAuthentication())
                .userStats(user.isUserState())
                .userBlock(user.isUserBlock())
                .userPause(user.isUserPause())
                .pushStart(user.isPushStart())
                .pushImminent(user.isPushImminent())
                .pushDayAgo(user.isPushDayAgo())
                .firstLogin(isFirstLogin)
                .build();
        return userResponse;
    }
}

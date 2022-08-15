package com.peoples.api.dto.response;

import com.peoples.api.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private String userId;
    private String nickname;
    private String img;
    private boolean emailAuthentication;
    private boolean userStats;
    private boolean userBlock;
    private boolean userPause;

    public static UserResponse from(User user){
        UserResponse userResponse = UserResponse.builder()
                                                .userId(user.getUserId())
                                                .nickname(user.getNickname())
                                                .img(user.getImg()).emailAuthentication(user.isEmailAuthentication())
                                                .userStats(user.isUserState())
                                                .userBlock(user.isUserBlock())
                                                .userPause(user.isUserPause())
                                                .build();

        return userResponse;
    }
}

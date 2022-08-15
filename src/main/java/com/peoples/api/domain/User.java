package com.peoples.api.domain;

import com.peoples.api.domain.enumeration.Role;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_USER")
public class User {

    @Id
    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "NICKNAME")
    private String nickname;

    @Column(name = "IMG")
    private String img;

    @Column(name = "ROLE")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "SNS_KAKAO")
    private boolean snsKakao;

    @Column(name = "SNS_NAVER")
    private boolean snsNaver;

    @CreatedDate
    @Column(name = "CREATED_AT", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "LAST_LOGIN_AT")
    private LocalDateTime lastLoginAt;

    @Column(name = "REFRESH_TOKEN")
    private String refreshToken;

    @Column(name = "EMAIL_AUTHENTICATION")
    private boolean emailAuthentication;

    @Column(name = "KICKOUT_CNT")
    private int kickoutCnt;

    @Column(name = "USER_STATE")
    private boolean userState; // 주의 회원

    @Column(name = "USER_BLOCK")
    private boolean userBlock; // 영구 정지

    @Column(name = "USER_PAUSE")
    private boolean userPause; // 일시 정지

    @OneToMany(mappedBy = "user")
    private List<StudyMember> studyMemberList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<UserSchedule> userScheduleList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<UserStudyHistory> userStudyHistoryList = new ArrayList<>();

    public void updateRefreshToken(String refreshToken){
        this.refreshToken = refreshToken;
    }

    public void updateLastLogin(LocalDateTime localDateTime){
        this.lastLoginAt = localDateTime;
    }

    public void updateProfileImg(String img){
        this. img = img;
    }

    public void updateNickname(String nickname){
        this.nickname = nickname;
    }
}

package com.peoples.api.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailAuthToken {

    private static final long EMAIL_TOKEN_EXPIRATION_TIME_VALUE = 20L;	//토큰 만료 시간 5분

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36)
    private String id;

    @Column
    private LocalDateTime expirationDate;

    @Column
    private boolean expired;

    @Column
    private String userId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    /**
     * 이메일 인증 토큰 생성
     * @param userId
     * @return
     */
    public static EmailAuthToken createEmailAuthToken(String userId){
        EmailAuthToken emailAuthToken = new EmailAuthToken();
        emailAuthToken.expirationDate = LocalDateTime.now().plusMinutes(EMAIL_TOKEN_EXPIRATION_TIME_VALUE);
        emailAuthToken.userId = userId;
        emailAuthToken.expired = false;
        return emailAuthToken;
    }

    /**
     * 토큰 사용으로 인한 만료
     */
    public void useToken(){
        expired = true;
    }
}

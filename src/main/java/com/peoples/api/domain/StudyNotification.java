package com.peoples.api.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder
@Table(name = "TB_STUDY_NOTIFICATION")
public class StudyNotification {

    @Id
    @Column(name = "NOTI_ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long notificationId;

    @Column(name = "NOTI_SUBJECT")
    private String notificationSubject;

    @Column(name = "NOTI_CONTENTS")
    private String notificationContents;

    @CreatedDate
    @Column(name = "CREATED_AT", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "PIN")
    private boolean pin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STUDY_ID")
    private Study study;

    public void updateSubject(String notificationSubject){
        this.notificationSubject = notificationSubject;
    }

    public void updateContents(String notificationContents){
        this.notificationContents = notificationContents;
    }

    public void updatePin(boolean pin){
        this.pin = pin;
    }
}

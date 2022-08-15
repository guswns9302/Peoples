package com.peoples.api.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STUDY_ID")
    private Study study;
}

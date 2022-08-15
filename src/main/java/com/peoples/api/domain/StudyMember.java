package com.peoples.api.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "TB_STUDY_MEMBER")
public class StudyMember {

    @Id
    @Column(name = "STUDY_MEMBER_ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long studyMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STUDY_ID")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "USER_MANAGER")
    private boolean userManager;

    @Column(name = "USER_ROLE")
    private String userRole;

    @Column(name = "DEPOSIT")
    private int deposit;

}

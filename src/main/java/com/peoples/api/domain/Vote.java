package com.peoples.api.domain;

import com.peoples.api.domain.enumeration.Status;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_VOTE")
public class Vote {

    @Id
    @Column(name = "VOTE_ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long voteId;

    @Column(name = "VOTE_NAME")
    private String voteName;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "VOTE_FINISH")
    private LocalDateTime voteFinish;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "START_DATETIME")
    private LocalDateTime startDatetime;

    @Column(name = "END_DATETIME")
    private LocalDateTime endDatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STUDY_ID")
    private Study study;

    @OneToMany(mappedBy = "vote")
    private List<VoteUser> voteUserList = new ArrayList<>();
}

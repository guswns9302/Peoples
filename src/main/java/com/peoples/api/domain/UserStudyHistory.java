package com.peoples.api.domain;

import com.peoples.api.domain.enumeration.ParticipationOperation;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_USER_STUDY_HISTORY")
public class UserStudyHistory {

    @Id
    @Column(name = "USH_ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long ushId;

    @Column(name = "STUDY_NAME")
    private String studyName;

    @Column(name = "START")
    private LocalDateTime start;

    @Column(name = "END")
    private LocalDateTime end;

    @Column(name = "PO")
    @Enumerated(EnumType.STRING)
    private ParticipationOperation po;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;
}

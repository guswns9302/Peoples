package com.peoples.api.domain;

import com.peoples.api.domain.enumeration.AttendStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_ATTENDANCE")
public class Attendance {

    @Id
    @Column(name = "TB_ATTENDANCE")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long attendanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEDULE_ID")
    private StudySchedule studySchedule;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "ATTENDANCE")
    @Enumerated(EnumType.STRING)
    private AttendStatus attendStatus;

    @Column(name = "REASON")
    private String reason;

    @Column(name = "FINE")
    private int fine;
}

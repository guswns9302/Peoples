package com.peoples.api.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_STUDY_SCHEDULE")
public class StudySchedule {

    @Id
    @Column(name = "TB_SCHEDULE_ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long studyScheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STUDY_ID")
    private Study study;

    @Column(name = "SCHEDULE_NAME")
    private String studyScheduleName;

    @Column(name = "SCHEDULE_DATE")
    private LocalDate studyScheduleDate;

    @Column(name = "SCHEDULE_PLACE")
    private String studySchedulePlace;

    @Column(name = "CHECK_NUMBER")
    private int checkNumber;

    @OneToMany(mappedBy = "studySchedule")
    private List<Attendance> attendanceList = new ArrayList<>();
}

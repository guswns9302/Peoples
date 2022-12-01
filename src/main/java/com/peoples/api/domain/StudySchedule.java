package com.peoples.api.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Builder
@AllArgsConstructor
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

    @Column(name = "SCHEDULE_START")
    private String studyScheduleStart;

    @Column(name = "SCHEDULE_END")
    private String studyScheduleEnd;

    @Column(name = "SCHEDULE_PLACE")
    private String studySchedulePlace;

    @Column(name = "CHECK_NUMBER")
    private int checkNumber;

    @Column(name = "REPEAT_NUMBER")
    private Long repeatNumber;

    @OneToMany(mappedBy = "studySchedule")
    private List<Attendance> attendanceList = new ArrayList<>();

    public void updateStudySchedule(Map<String, Object> param){
        this.studyScheduleDate = LocalDate.parse(param.get("studyScheduleDate").toString());
        this.studyScheduleName = param.get("studyScheduleName").toString();
        this.studyScheduleStart = param.get("studyScheduleStart").toString();
        this.studyScheduleEnd = param.get("studyScheduleEnd").toString();
        this.studySchedulePlace = param.get("studySchedulePlace").toString();
    }

    public void repeatNumberIn (Long number){
        this.repeatNumber = number;
    }
}

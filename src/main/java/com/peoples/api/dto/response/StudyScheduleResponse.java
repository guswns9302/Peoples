package com.peoples.api.dto.response;

import com.peoples.api.domain.StudySchedule;
import com.peoples.api.domain.UserSchedule;
import com.peoples.api.domain.enumeration.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Builder
@Getter
@ToString
public class StudyScheduleResponse {
    private Long studyScheduleId;
    private String studyScheduleName;
    private LocalDate studyScheduleDate;
    private String studyScheduleStart;
    private String studyScheduleEnd;
    private String studySchedulePlace;
    private String studyName;

    public static StudyScheduleResponse from (StudySchedule studySchedule){
        return StudyScheduleResponse.builder()
                .studyScheduleId(studySchedule.getStudyScheduleId())
                .studyScheduleName(studySchedule.getStudyScheduleName())
                .studyScheduleDate(studySchedule.getStudyScheduleDate())
                .studyScheduleStart(studySchedule.getStudyScheduleStart())
                .studyScheduleEnd(studySchedule.getStudyScheduleEnd())
                .studySchedulePlace(studySchedule.getStudySchedulePlace())
                .studyName(studySchedule.getStudy().getStudyName())
                .build();
    }
}

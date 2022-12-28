package com.peoples.api.dto.response;

import com.peoples.api.domain.StudySchedule;
import com.peoples.api.domain.UserSchedule;
import com.peoples.api.domain.enumeration.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Builder
@Getter
@ToString
public class StudyScheduleResponse {
    private Long studyScheduleId;
    private String studyScheduleName;
    private LocalDateTime studyScheduleStartDateTime;
    private LocalDateTime studyScheduleEndDateTime;
    private String studySchedulePlace;
    private String studyName;

    public static StudyScheduleResponse from (StudySchedule studySchedule){
        LocalDateTime startDateTime =
                LocalDateTime.parse(
                        studySchedule.getStudyScheduleDate() + " " + studySchedule.getStudyScheduleStart() + ":00",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );

        LocalDateTime endDateTime =
                LocalDateTime.parse(
                        studySchedule.getStudyScheduleDate() + " " + studySchedule.getStudyScheduleEnd() + ":00",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );
        return StudyScheduleResponse.builder()
                .studyScheduleId(studySchedule.getStudyScheduleId())
                .studyScheduleName(studySchedule.getStudyScheduleName())
                .studyScheduleStartDateTime(startDateTime)
                .studyScheduleEndDateTime(endDateTime)
                .studySchedulePlace(studySchedule.getStudySchedulePlace())
                .studyName(studySchedule.getStudy().getStudyName())
                .build();
    }
}

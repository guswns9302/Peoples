package com.peoples.api.dto.response;

import com.peoples.api.domain.UserSchedule;
import com.peoples.api.domain.enumeration.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Builder
@Getter
@ToString
public class UserScheduleResponse {
    private Long scheduleId;
    private String scheduleName;
    private LocalDate scheduleDate;
    private Status status;

    public static UserScheduleResponse from(UserSchedule userSchedule){
        UserScheduleResponse userScheduleResponse = UserScheduleResponse.builder()
                                                                        .scheduleId(userSchedule.getScheduleId())
                                                                        .scheduleName(userSchedule.getScheduleName())
                                                                        .scheduleDate(userSchedule.getScheduleDate())
                                                                        .status(userSchedule.getStatus())
                                                                        .build();
        return userScheduleResponse;
    }
}

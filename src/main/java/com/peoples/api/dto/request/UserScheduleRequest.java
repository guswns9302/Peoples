package com.peoples.api.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Builder
@ToString
public class UserScheduleRequest {
    private String scheduleName;
    private LocalDate scheduleDate;
}

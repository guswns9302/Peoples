package com.peoples.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.peoples.api.domain.Alarm;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
public class AlarmResponse {

    private String contents;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private String studyName;

    public static AlarmResponse from(Alarm alarm){
        return AlarmResponse.builder()
                .contents(alarm.getContents())
                .createdAt(alarm.getCreatedAt())
                .studyName(alarm.getStudy().getStudyName())
                .build();
    }
}

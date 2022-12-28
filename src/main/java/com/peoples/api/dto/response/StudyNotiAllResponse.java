package com.peoples.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.peoples.api.domain.StudyNotification;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
public class StudyNotiAllResponse {
    private Long notificationId;
    private String notificationSubject;
    private String notificationContents;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private boolean pin;

    public static StudyNotiAllResponse from(StudyNotification studyNotification){
        return StudyNotiAllResponse.builder()
                .notificationId(studyNotification.getNotificationId())
                .notificationSubject(studyNotification.getNotificationSubject())
                .notificationContents(studyNotification.getNotificationContents())
                .createdAt(studyNotification.getCreatedAt())
                .pin(studyNotification.isPin())
                .build();
    }
}

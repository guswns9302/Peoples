package com.peoples.api.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Builder
public class StudyNotiRequest {
    private String notificationSubject;
    private String notificationContents;
    private Long studyId;
}

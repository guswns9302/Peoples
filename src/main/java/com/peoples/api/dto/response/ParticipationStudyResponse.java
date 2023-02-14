package com.peoples.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.peoples.api.domain.Study;
import com.peoples.api.domain.enumeration.StudyCategory;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class ParticipationStudyResponse {
    private long studyId;
    private String studyName;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime finishAt;


    public static ParticipationStudyResponse from(Study study){
        ParticipationStudyResponse studyResponse = ParticipationStudyResponse.builder()
                .studyId(study.getStudyId())
                .studyName(study.getStudyName())
                .createdAt(study.getCreatedAt())
                .finishAt(study.getFinishAt())
                .build();
        return studyResponse;
    }
}

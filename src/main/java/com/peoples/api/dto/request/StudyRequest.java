package com.peoples.api.dto.request;

import com.peoples.api.domain.enumeration.StudyCategory;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@Builder
@ToString
public class StudyRequest {
    private String studyName;
    private StudyCategory studyCategory;
    private boolean studyOn;
    private boolean studyOff;
    private String studyInfo;
    private Map<String, Object> studyRule;
    private String studyFlow;
    private String studyRegion;
}

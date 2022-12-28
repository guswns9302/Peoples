package com.peoples.api.dto.response;

import com.peoples.api.domain.Study;
import com.peoples.api.domain.enumeration.StudyCategory;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class StudyResponse {
    private long studyId;
    private String studyName;
    private StudyCategory studyCategory;
    private Map<String,Object> studyRule;
    private String studyFlow;
    private boolean studyOn;
    private boolean studyOff;
    private String studyInfo;
    private boolean studyBlock;
    private boolean studyPause;


    public static StudyResponse from(Study study){
        StudyResponse studyResponse = StudyResponse.builder()
                .studyId(study.getStudyId())
                .studyName(study.getStudyName())
                .studyCategory(study.getStudyCategory())
                .studyRule(study.getStudyRule())
                .studyFlow(study.getStudyFlow())
                .studyOn(study.isStudyOn())
                .studyOff(study.isStudyOff())
                .studyInfo(study.getStudyInfo())
                .studyPause(study.isStudyPause())
                .studyBlock(study.isStudyBlock())
                .build();
        return studyResponse;
    }
}

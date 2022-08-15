package com.peoples.api.dto.response;

import com.peoples.api.domain.Study;
import com.peoples.api.domain.enumeration.Onoff;
import com.peoples.api.domain.enumeration.StudyCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudyResponse {
    private long studyId;
    private String studyName;
    private StudyCategory studyCategory;
    private Onoff onoff;
    private String studyInfo;
    private boolean studyBlock;
    private boolean studyPause;


    public static StudyResponse from(Study study){
        StudyResponse studyResponse = StudyResponse.builder()
                .studyId(study.getStudyId())
                .studyName(study.getStudyName())
                .studyCategory(study.getStudyCategory())
                .onoff(study.getOnoff())
                .studyInfo(study.getStudyInfo())
                .studyPause(study.isStudyPause())
                .studyBlock(study.isStudyBlock())
                .build();
        return studyResponse;
    }
}

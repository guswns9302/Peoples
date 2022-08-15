package com.peoples.api.dto.response;

import com.peoples.api.domain.UserStudyHistory;
import com.peoples.api.domain.enumeration.ParticipationOperation;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
public class UserStudyHistoryResponse {

    private String studyName;
    private LocalDateTime start;
    private LocalDateTime end;
    private ParticipationOperation po;

    public static UserStudyHistoryResponse from(UserStudyHistory userStudyHistory){
        UserStudyHistoryResponse userStudyHistoryResponse = UserStudyHistoryResponse.builder()
                                                                                    .studyName(userStudyHistory.getStudyName())
                                                                                    .start(userStudyHistory.getStart())
                                                                                    .end(userStudyHistory.getEnd())
                                                                                    .po(userStudyHistory.getPo())
                                                                                    .build();
        return userStudyHistoryResponse;
    }
}

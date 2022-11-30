package com.peoples.api.dto.response;

import com.peoples.api.domain.Study;
import com.peoples.api.domain.StudyMember;
import com.peoples.api.domain.User;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.persistence.*;

@Getter
@Builder
public class StudyMemberResponse {
    private Long studyMemberId;
    private String userNickname;
    private String img;
    private boolean userManager;
    private String userRole;
    private int deposit;

    public static StudyMemberResponse from(StudyMember studyMember){
        String fileName = "fileName";
        return StudyMemberResponse.builder()
                .studyMemberId(studyMember.getStudyMemberId())
                .userNickname(studyMember.getUser().getNickname())
                .img(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/downloadIMG").queryParam(fileName, studyMember.getUser().getImg()).toUriString())
                .userManager(studyMember.isUserManager())
                .userRole(studyMember.getUserRole())
                .deposit(studyMember.getDeposit())
                .build();
    }
}

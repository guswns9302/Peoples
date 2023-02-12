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
    private boolean userMaster;
    private String userRole;
    private int deposit;

    public static StudyMemberResponse from(StudyMember studyMember){
        String fileName = "fileName";
        if(studyMember.getUserRole().equals("스터디장")){
            return StudyMemberResponse.builder()
                    .studyMemberId(studyMember.getStudyMemberId())
                    .userNickname(studyMember.getUser().getNickname())
                    .img(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/downloadIMG").queryParam(fileName, studyMember.getUser().getImg()).toUriString())
                    .userManager(studyMember.isUserManager())
                    .userMaster(true)
                    .userRole(studyMember.getUserRole())
                    .deposit(studyMember.getDeposit())
                    .build();
        }
        else{
            return StudyMemberResponse.builder()
                    .studyMemberId(studyMember.getStudyMemberId())
                    .userNickname(studyMember.getUser().getNickname())
                    .img(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/downloadIMG").queryParam(fileName, studyMember.getUser().getImg()).toUriString())
                    .userManager(studyMember.isUserManager())
                    .userMaster(false)
                    .userRole(studyMember.getUserRole())
                    .deposit(studyMember.getDeposit())
                    .build();
        }
    }
}

package com.peoples.api.service;

import com.peoples.api.domain.Study;
import com.peoples.api.domain.StudyMember;
import com.peoples.api.domain.StudyNotification;
import com.peoples.api.domain.UserStudyHistory;
import com.peoples.api.domain.enumeration.ParticipationOperation;
import com.peoples.api.domain.enumeration.Status;
import com.peoples.api.domain.security.SecurityUser;
import com.peoples.api.dto.request.StudyRequest;
import com.peoples.api.dto.response.StudyResponse;
import com.peoples.api.exception.CustomException;
import com.peoples.api.exception.ErrorCode;
import com.peoples.api.repository.StudyMemberRepository;
import com.peoples.api.repository.StudyRepository;
import com.peoples.api.repository.UserRepository;
import com.peoples.api.repository.UserStudyHistoryRepository;
import com.peoples.api.service.responseMap.ResponseMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyService extends ResponseMap {

    private final UserRepository userRepository;
    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final UserStudyHistoryRepository userStudyHistoryRepository;

    @Transactional
    public Map<String,Object> create(StudyRequest param, SecurityUser user) {
        log.debug("study create param : {}, userId : {}", param, user.getUsername());
        Study study = Study.builder()
                .studyName(param.getStudyName())
                .onoff(param.getOnoff())
                .studyCategory(param.getStudyCategory())
                .studyInfo(param.getStudyInfo())
                .studyRule(param.getStudyRule())
                .studyFlow(param.getStudyFlow())
                .status(Status.RUNNING)
                .studyBlock(false)
                .studyPause(false)
                .build();

        Study createStudy = studyRepository.save(study);

        if(createStudy != null){
            StudyMember studyMember = StudyMember.builder()
                                                 .user(user.getUser())
                                                 .study(createStudy)
                                                 .userManager(true)
                                                 .userRole("스터디장")
                                                 .deposit(Integer.parseInt(createStudy.getStudyRule().get("deposit").toString()))
                                                 .build();

            UserStudyHistory userStudyHistory = UserStudyHistory.builder()
                                                                .user(user.getUser())
                                                                .studyName(createStudy.getStudyName())
                                                                .start(createStudy.getCreatedAt())
                                                                .po(ParticipationOperation.OPERATION)
                                                                .build();

            if(studyMemberRepository.save(studyMember) != null && userStudyHistoryRepository.save(userStudyHistory) != null){
                return this.responseMap("스터디 생성에 성공하였습니다.", StudyResponse.from(createStudy));
            }
            else{
                throw new CustomException(ErrorCode.USER_NOT_FOUND);
            }
        }
        else{
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    public Map<String,Object> findAll(String userId) {
        List<StudyMember> studyByUserId = userRepository.findByUserId(userId).get().getStudyMemberList();

        if(!studyByUserId.isEmpty()){
            List<StudyResponse> joinStudyList = new ArrayList<>();
            studyByUserId.forEach(data->{
                joinStudyList.add(StudyResponse.from(data.getStudy()));
            });

            return this.responseMap("참여 중인 스터디 목록 조회", joinStudyList);
        }
        else{
            return this.responseMap("참여 중인 스터디가 없습니다.", null);
        }
    }

    public Map<String,Object> findStudy(Long studyId) {
        Optional<Study> study = studyRepository.findById(studyId);
        if(study.isPresent()){
            Map<String,Object> result = new HashMap<>();
            result.put("study", StudyResponse.from(study.get()));
            List<StudyNotification> studyNotificationList = study.get().getStudyNotificationList();
            if(studyNotificationList.isEmpty()){
                result.put("notification", null);
            }
            else{
                result.put("notification", studyNotificationList.get(studyNotificationList.size()-1));
            }
            return this.responseMap("스터디 조회", result);
        }
        else{
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }
}

package com.peoples.api.service;

import com.peoples.api.domain.Study;
import com.peoples.api.domain.StudyMember;
import com.peoples.api.domain.StudyNotification;
import com.peoples.api.domain.UserStudyHistory;
import com.peoples.api.domain.enumeration.ParticipationOperation;
import com.peoples.api.domain.enumeration.Status;
import com.peoples.api.domain.security.SecurityUser;
import com.peoples.api.dto.request.StudyNotiRequest;
import com.peoples.api.dto.request.StudyRequest;
import com.peoples.api.dto.response.StudyNotiAllResponse;
import com.peoples.api.dto.response.StudyResponse;
import com.peoples.api.exception.CustomException;
import com.peoples.api.exception.ErrorCode;
import com.peoples.api.repository.*;
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
    private final StudyNotificationRepository studyNotificationRepository;

    private Optional<Study> getStudy(Long studyId){
        return studyRepository.findById(studyId);
    }

    @Transactional
    public Map<String,Object> create(StudyRequest param, SecurityUser user) {
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

    public Map<String,Object> findStudy(Long studyId, SecurityUser user) {
        Optional<Study> study = this.getStudy(studyId);
        if(study.isPresent()){
            Map<String,Object> result = new HashMap<>();
            result.put("study", StudyResponse.from(study.get()));

            study.get().getStudyMemberList().forEach(x->{
                if(x.getUserRole().equals("스터디장")){
                    if(x.getUser().getUserId().equals(user.getUser().getUserId())){
                        result.put("master", user.getUser().getUserId());
                    }
                }

                if(x.isUserManager()){
                    if(x.getUser().getUserId().equals(user.getUser().getUserId())){
                        result.put("manager", true);
                    }
                }
            });

            Optional<StudyNotification> existNoti = studyNotificationRepository.findByPin(true);
            if(existNoti.isEmpty()){
                result.put("notification", null);
            }
            else{
                result.put("notification", StudyNotiAllResponse.from(existNoti.get()));
            }
            return this.responseMap("스터디 조회", result);
        }
        else{
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public Map<String,Object> findStudyNoti(Long studyId) {
        Optional<Study> study = this.getStudy(studyId);
        if(study.isPresent()){
            List<StudyNotiAllResponse> notiList = new ArrayList<>();
            study.get().getStudyNotificationList().forEach(list->{
               notiList.add(StudyNotiAllResponse.from(list));
            });
            return this.responseMap("스터디 공지 전체 조회", notiList);
        }
        else{
            throw new CustomException(ErrorCode.STUDY_NOT_FOUND);
        }
    }

    @Transactional
    public Map<String,Object> createStudyNoti(StudyNotiRequest studyNotiRequest) {
        Optional<Study> study = this.getStudy(studyNotiRequest.getStudyId());

        StudyNotification newNotification = StudyNotification.builder()
                .study(study.get())
                .notificationSubject(studyNotiRequest.getNotificationSubject())
                .notificationContents(studyNotiRequest.getNotificationContents())
                .pin(false)
                .build();

        studyNotificationRepository.save(newNotification);

        return this.findStudyNoti(studyNotiRequest.getStudyId());
    }

    @Transactional
    public Map<String,Object> updateStudyNoti(Map<String, Object> param) {
        Optional<StudyNotification> findNoti = studyNotificationRepository.findById(Long.parseLong(param.get("notificationId").toString()));
        if(findNoti.isPresent()){
            findNoti.get().updateSubject(param.get("notificationSubject").toString());
            findNoti.get().updateContents(param.get("notificationContents").toString());

            return this.findStudyNoti(findNoti.get().getStudy().getStudyId());
        }
        else{
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }

    @Transactional
    public Map<String,Object> updatePin(Map<String, Object> param) {
        Optional<StudyNotification> findNoti = studyNotificationRepository.findById(Long.parseLong(param.get("notificationId").toString()));
        if(findNoti.isPresent()){
            if(param.get("pin").toString().equals("true")){
                Optional<StudyNotification> existPin = studyNotificationRepository.findByPin(true);
                if(existPin.isEmpty()){
                    findNoti.get().updatePin(true);
                    return this.findStudyNoti(findNoti.get().getStudy().getStudyId());
                }
                else{
                    throw new CustomException(ErrorCode.EXIST_PIN_NOTIFICATION);
                }
            }
            else{
                findNoti.get().updatePin(false);
                return this.findStudyNoti(findNoti.get().getStudy().getStudyId());
            }
        }
        else{
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }

    @Transactional
    public Map<String,Object> deleteStudyNoti(Long notificationId) {
        Optional<StudyNotification> findNoti = studyNotificationRepository.findById(notificationId);
        if(findNoti.isPresent()){
            Long studyId = findNoti.get().getStudy().getStudyId();
            studyNotificationRepository.delete(findNoti.get());
            studyNotificationRepository.flush();
            return this.findStudyNoti(studyId);
        }
        else{
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }
}

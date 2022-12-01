package com.peoples.api.service;

import com.peoples.api.domain.*;
import com.peoples.api.domain.enumeration.AttendStatus;
import com.peoples.api.dto.response.AttendanceResponse;
import com.peoples.api.exception.CustomException;
import com.peoples.api.exception.ErrorCode;
import com.peoples.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;
    private final StudyRepository studyRepository;

    @Transactional
    public void changeManager(StudyMember studyMember) {
        Alarm alarm;
        if(studyMember.isUserManager()){
            alarm = Alarm.builder()
                    .contents("관리자 권한이 생겼어요.")
                    .user(studyMember.getUser())
                    .study(studyMember.getStudy())
                    .build();
        }
        else{
            alarm = Alarm.builder()
                    .contents("관리자 권한이 사라졌어요.")
                    .user(studyMember.getUser())
                    .study(studyMember.getStudy())
                    .build();
        }
        alarmRepository.save(alarm);
    }

    @Transactional
    public void expire(StudyMember studyMember) {
        Alarm alarm = Alarm.builder()
                .contents("스터디에서 강퇴됐어요.")
                .user(studyMember.getUser())
                .study(studyMember.getStudy())
                .build();
        alarmRepository.save(alarm);
    }

    @Transactional
    public void studyEnd(Study study) {
        study.getStudyMemberList().forEach(list->{
            Alarm alarm = Alarm.builder()
                    .contents("스터디 이/가 종료됐어요.")
                    .user(list.getUser())
                    .study(study)
                    .build();
            alarmRepository.save(alarm);
        });
    }

    @Transactional
    public void updateAttend(Attendance attendance) {
        Optional<User> byUserId = userRepository.findByUserId(attendance.getUserId());
        Alarm alarm = Alarm.builder()
                .contents(attendance.getStudySchedule().getStudyScheduleDate() + " 출결이 변경됐어요.")
                .user(byUserId.get())
                .study(attendance.getStudySchedule().getStudy())
                .build();
        alarmRepository.save(alarm);
    }

    @Transactional
    public void expireConfirm(String userId, Study study){
        study.getStudyMemberList().forEach(memberList->{
            if(memberList.isUserManager()){
                Alarm alarm = Alarm.builder()
                        .contents(userId + "님이 강퇴 조건에 도달했어요.")
                        .user(memberList.getUser())
                        .study(study)
                        .build();
                alarmRepository.save(alarm);
            }
        });
    }
}

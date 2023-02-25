package com.peoples.api.service;

import com.peoples.api.domain.*;
import com.peoples.api.domain.enumeration.AttendStatus;
import com.peoples.api.domain.enumeration.ParticipationOperation;
import com.peoples.api.domain.enumeration.Status;
import com.peoples.api.domain.security.SecurityUser;
import com.peoples.api.dto.request.StudyNotiRequest;
import com.peoples.api.dto.request.StudyRequest;
import com.peoples.api.dto.response.ParticipationStudyResponse;
import com.peoples.api.dto.response.StudyNotiAllResponse;
import com.peoples.api.dto.response.StudyResponse;
import com.peoples.api.dto.response.StudyScheduleResponse;
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
public class StudyService {

    private final UserRepository userRepository;
    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final UserStudyHistoryRepository userStudyHistoryRepository;
    private final StudyNotificationRepository studyNotificationRepository;
    private final StudyScheduleRepository studyScheduleRepository;
    private final AlarmService alarmService;

    private Optional<Study> getStudy(Long studyId){
        Optional<Study> isStudy = studyRepository.findById(studyId);
        if(isStudy.isEmpty()){
            throw new CustomException(ErrorCode.STUDY_NOT_FOUND);
        }
        else{
            return isStudy;
        }
    }

    @Transactional
    public StudyResponse create(StudyRequest param, SecurityUser user) {
        Study study = Study.builder()
                .studyName(param.getStudyName())
                .studyOn(param.isStudyOn())
                .studyOff(param.isStudyOff())
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
                return StudyResponse.from(createStudy);
            }
            else{
                throw new CustomException(ErrorCode.USER_NOT_FOUND);
            }
        }
        else{
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    public List<StudyResponse> findAll(String userId) {
        List<StudyMember> studyByUserId = userRepository.findByUserId(userId).get().getStudyMemberList();
        List<StudyResponse> joinStudyList = new ArrayList<>();
        if(!studyByUserId.isEmpty()){
            studyByUserId.forEach(data->{
                if(data.getStudy().getStatus().equals(Status.RUNNING)){
                    joinStudyList.add(StudyResponse.from(data.getStudy()));
                }
            });
        }
        return joinStudyList;
    }

    public List<ParticipationStudyResponse> findParticipationStudyList(String userId) {
        List<StudyMember> studyByUserId = userRepository.findByUserId(userId).get().getStudyMemberList();
        List<ParticipationStudyResponse> finishStudyList = new ArrayList<>();
        if(!studyByUserId.isEmpty()){
            studyByUserId.forEach(data->{
                finishStudyList.add(ParticipationStudyResponse.from(data.getStudy()));
            });
        }
        return finishStudyList;
    }

    public Map<String,Object> findStudy(Long studyId, SecurityUser user) {
        Optional<Study> study = this.getStudy(studyId);
        if(study.isPresent()){
            Map<String,Object> result = new HashMap<>();
            result.put("study", StudyResponse.from(study.get()));

            study.get().getStudyMemberList().forEach(x->{
                if(x.getUserRole().equals("스터디장")){
                    if(x.getUser().getUserId().equals(user.getUser().getUserId())){
                        result.put("master", true);
                        result.put("masterNickName", user.getUser().getNickname());
                    }
                    else{
                        result.put("master", false);
                    }
                }

                if(x.isUserManager()){
                    if(x.getUser().getUserId().equals(user.getUser().getUserId())){
                        result.put("manager", true);
                    }
                    else{
                        result.put("manager", false);
                    }
                }
            });

            Optional<StudyNotification> existNoti = studyNotificationRepository.findByPinAndStudy_StudyId(true,studyId);
            if(existNoti.isEmpty()){
                result.put("notification", null);
            }
            else{
                result.put("notification", StudyNotiAllResponse.from(existNoti.get()));
            }

            List<Long> nearDay = new ArrayList<>();
            List<StudySchedule> studyScheduleList = new ArrayList<>();
            study.get().getStudyScheduleList().forEach(x->{
                // 스터디 날짜 및 시작 시간 조합
                LocalDate scheduleDate = x.getStudyScheduleDate();
                String startDateStr = scheduleDate.toString() + " " + x.getStudyScheduleStart();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime scheduleDateTimeStart = LocalDateTime.parse(startDateStr, formatter);

                // 스터디 날짜 및 종료 시간 조합
                String endDateStr = scheduleDate.toString() + " " + x.getStudyScheduleEnd();
                LocalDateTime scheduleDateTimeEnd = LocalDateTime.parse(endDateStr, formatter);

                // 현재시간
                LocalDateTime current = LocalDateTime.now();
                LocalDateTime now = LocalDateTime.parse(current.format(formatter), formatter);
                log.debug("현재 시간 : {}", now);
                log.debug("스터디 시작 시간 : {}", scheduleDateTimeStart);
                log.debug("스터디 종료 시간 : {}", scheduleDateTimeEnd);

                if(now.isBefore(scheduleDateTimeEnd)){
                    Duration duration = Duration.between(now,scheduleDateTimeStart);
                    nearDay.add(duration.getSeconds());
                    studyScheduleList.add(x);
                }

            });
            long min = 0;
            if(nearDay.size() > 0){
                min = Collections.min(nearDay);
                result.put("studySchedule",StudyScheduleResponse.from(studyScheduleList.get(nearDay.indexOf(min))));
                /*if(min < 0){
                    min = 0;
                }
                else{
                    min = min / 60 / 60;
                }*/
                result.put("dayCnt", min);
            }
            else{
                result.put("dayCnt", min);
                result.put("studySchedule",null);
            }

            List<Integer> totalFine = new ArrayList<>();
            List<Integer> totalAttendance = new ArrayList<>();
            List<Integer> totalLateness = new ArrayList<>();
            List<Integer> totalAbsent = new ArrayList<>();
            List<Integer> totalHold = new ArrayList<>();
            study.get().getStudyScheduleList().forEach(x->{
                x.getAttendanceList().forEach(y->{
                    totalFine.add(y.getFine());
                    if(y.getAttendStatus().equals(AttendStatus.ATTENDANCE)){
                        totalAttendance.add(1);
                    }
                    else if(y.getAttendStatus().equals(AttendStatus.LATENESS)){
                        totalLateness.add(1);
                    }
                    else if(y.getAttendStatus().equals(AttendStatus.ABSENT)){
                        totalAbsent.add(1);
                    }
                    else if(y.getAttendStatus().equals(AttendStatus.HOLD)){
                        totalHold.add(1);
                    }
                });
            });

            int fine = totalFine.stream().mapToInt(Integer::intValue).sum();
            int attendanceCnt = totalAttendance.stream().mapToInt(Integer::intValue).sum();
            int latenessCnt = totalLateness.stream().mapToInt(Integer::intValue).sum();
            int absentCnt = totalAbsent.stream().mapToInt(Integer::intValue).sum();
            int holdCnt = totalHold.stream().mapToInt(Integer::intValue).sum();

            result.put("totalFine",fine);
            result.put("attendanceCnt",attendanceCnt);
            result.put("latenessCnt",latenessCnt);
            result.put("absentCnt",absentCnt);
            result.put("holdCnt",holdCnt);

            return result;
        }
        else{
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public List<StudyNotiAllResponse> findStudyNoti(Long studyId) {
        Optional<Study> study = this.getStudy(studyId);
        if(study.isPresent()){
            List<StudyNotiAllResponse> notiList = new ArrayList<>();
            study.get().getStudyNotificationList().forEach(list->{
               notiList.add(StudyNotiAllResponse.from(list));
            });
            return notiList;
        }
        else{
            throw new CustomException(ErrorCode.STUDY_NOT_FOUND);
        }
    }

    @Transactional
    public List<StudyNotiAllResponse> createStudyNoti(StudyNotiRequest studyNotiRequest) {
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
    public List<StudyNotiAllResponse> updateStudyNoti(Map<String, Object> param) {
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
    public List<StudyNotiAllResponse> updatePin(Map<String, Object> param) {
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
    public List<StudyNotiAllResponse> deleteStudyNoti(Long notificationId) {
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

    @Transactional
    public boolean join(long studyId, User user) {
        Optional<Study> study = this.getStudy(studyId);
        if(study.isPresent()){
            if(study.get().getStatus().toString().equals("STOP")){
                return false;
            }
            else{
                Optional<StudyMember> existMember = studyMemberRepository.findByUser_UserIdAndStudy_StudyId(user.getUserId(), studyId);
                if(existMember.isEmpty()){
                    StudyMember studyMember = StudyMember.builder()
                            .user(user)
                            .study(study.get())
                            .userManager(false)
                            .userRole("")
                            .deposit(Integer.parseInt(study.get().getStudyRule().get("deposit").toString()))
                            .build();

                    UserStudyHistory userStudyHistory = UserStudyHistory.builder()
                            .user(user)
                            .studyName(study.get().getStudyName())
                            .start(study.get().getCreatedAt())
                            .po(ParticipationOperation.PARTICIPATION)
                            .build();

                    studyMemberRepository.save(studyMember);
                    userStudyHistoryRepository.save(userStudyHistory);
                    return true;
                }
                else{
                    if(existMember.get().getUserRole().equals("스터디장")){
                        throw new CustomException(ErrorCode.ALREADY_STUDY_MASTER);
                    }
                    else{
                        throw new CustomException(ErrorCode.DUPLICATE_STUDY_MEMBER);
                    }
                }
            }
        }
        else{
            throw new CustomException(ErrorCode.STUDY_NOT_FOUND);
        }
    }

    @Transactional
    public StudyResponse updateStudy(long studyId, StudyRequest param) {
        Optional<Study> study = studyRepository.findById(studyId);
        if(study.isPresent()){
            study.get().updateStudyInfo(param);
            return StudyResponse.from(study.get());
        }
        else{
            throw new CustomException(ErrorCode.STUDY_NOT_FOUND);
        }
    }

    @Transactional
    public boolean finishStudy(long studyId) {
        Optional<Study> study = studyRepository.findById(studyId);
        if(study.isPresent()){
            study.get().finish(Status.STOP);
            study.get().setFinishAt(LocalDateTime.now());

            // 현재시간
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime current = LocalDateTime.now();
            LocalDateTime now = LocalDateTime.parse(current.format(formatter), formatter);

            study.get().getStudyScheduleList().forEach(list->{
                // 스터디 날짜 및 종료 시간 조합
                LocalDate scheduleDate = list.getStudyScheduleDate();
                String startDateStr = scheduleDate.toString() + " " + list.getStudyScheduleStart();
                LocalDateTime scheduleDateTimeEnd = LocalDateTime.parse(startDateStr, formatter);

                if(now.isBefore(scheduleDateTimeEnd)){
                    studyScheduleRepository.delete(list);
                }
            });
            alarmService.studyEnd(study.get());
            return true;
        }
        else{
            throw new CustomException(ErrorCode.STUDY_NOT_FOUND);
        }
    }

    @Transactional
    public List<StudyNotiAllResponse> updatePinCompulsion(Long notificationId) {
        Optional<StudyNotification> findNoti = studyNotificationRepository.findById(notificationId);
        if(findNoti.isPresent()){
            findNoti.get().getStudy().getStudyNotificationList().forEach(notiList->{
                notiList.updatePin(false);
            });
            findNoti.get().updatePin(true);
            return this.findStudyNoti(findNoti.get().getStudy().getStudyId());
        }
        else{
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }
}

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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final StudyRepository studyRepository;
    private final StudyScheduleRepository studyScheduleRepository;
    private final AttendanceRepository attendanceRepository;
    private final AlarmService alarmService;
    private final UserService userservice;
    private final StudyMemberRepository studyMemberRepository;

    @Transactional
    public AttendanceResponse attendSchedule(String userId, Map<String, Object> param) {
        AttendanceResponse response = new AttendanceResponse();

        Optional<StudySchedule> studySchedule = studyScheduleRepository.findById(Long.parseLong(param.get("studyScheduleId").toString()));
        if(studySchedule.isPresent()){
            Map<String, Object> isMember = new HashMap<>();
            studySchedule.get().getStudy().getStudyMemberList().forEach(x->{
                if(userId.equals(x.getUser().getUserId())) {
                    isMember.put("userId", userId);
                }
            });

            if(isMember.isEmpty()){
                throw new CustomException(ErrorCode.NOT_STUDY_MEMBER);
            }
            else{
                response.setUserId(userId);
                if(studySchedule.get().getCheckNumber() == Integer.parseInt(param.get("checkNumber").toString())){
                    // 스터디 날짜 및 시작 시간 조합
                    LocalDate scheduleDate = studySchedule.get().getStudyScheduleDate();
                    String startDateStr = scheduleDate.toString() + " " + studySchedule.get().getStudyScheduleStart();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime scheduleDateTimeStart = LocalDateTime.parse(startDateStr, formatter);

                    // 스터디 날짜 및 종료 시간 조합
                    String endDateStr = scheduleDate.toString() + " " + studySchedule.get().getStudyScheduleEnd();
                    LocalDateTime scheduleDateTimeEnd = LocalDateTime.parse(endDateStr, formatter);

                    // 출석체크 시점 현재시간
                    LocalDateTime current = LocalDateTime.now();
                    LocalDateTime now = LocalDateTime.parse(current.format(formatter), formatter);

                    log.debug("현재 시간 : {}", now);
                    log.debug("스터디 시간 : {}", scheduleDateTimeStart);
                    Map<String,Object> studyRule = studySchedule.get().getStudy().getStudyRule();
                    log.debug("스터디 규칙 : {}", studyRule);
                    // 지각 시간
                    HashMap lateness = (HashMap) studyRule.get("lateness");
                    int latenessTime = Integer.parseInt(lateness.get("time").toString());
                    int latenessFine = Integer.parseInt(lateness.get("fine").toString());
                    int latenessCnt = Integer.parseInt(lateness.get("count").toString());
                    // 결석 시간
                    HashMap absent = (HashMap) studyRule.get("absent");
                    int absentTime = Integer.parseInt(absent.get("time").toString());
                    int absentFine = Integer.parseInt(absent.get("fine").toString());

                    if(latenessTime > 0 && absentTime > 0){
                        // 규칙이 있을 때
                        if(now.isBefore(scheduleDateTimeStart.plusMinutes(latenessTime)) || now.isEqual(scheduleDateTimeStart.plusMinutes(latenessTime))){
                            // 지각 시간 전 출석
                            log.debug("지각 시간 전 출석");
                            response.setFine(0);
                            response.setAttendStatus(AttendStatus.ATTENDANCE);
                        }
                        else if(now.isAfter(scheduleDateTimeStart.plusMinutes(latenessTime)) && now.isBefore(scheduleDateTimeStart.plusMinutes(absentTime + 1))){
                            // 지각 시간 후 출석
                            log.debug("지각 시간 후 출석");
                            Duration duration = Duration.between(scheduleDateTimeStart.plusMinutes(latenessTime), now);
                            // 경과 된 초
                            int cnt = (int) duration.getSeconds() / (latenessCnt * 60);
                            response.setFine(latenessFine * cnt);
                            response.setAttendStatus(AttendStatus.LATENESS);
                        }
                        else if(now.isAfter(scheduleDateTimeStart.plusMinutes(absentTime))){
                            // 결석 시간 후 출석
                            log.debug("결석 시간 후 출석");
                            response.setFine(absentFine);
                            response.setAttendStatus(AttendStatus.ABSENT);
                        }
                    }
                    else{
                        // 규칙이 없을 때
                        if(now.isBefore(scheduleDateTimeEnd)){
                            // 종료 시간 전 출석
                            log.debug("종료 시간 전 출석");
                            response.setFine(0);
                            response.setAttendStatus(AttendStatus.ATTENDANCE);
                        }
                        else if(now.isAfter(scheduleDateTimeEnd)){
                            // 종료 시간 후 출석
                            log.debug("종료 시간 후 출석");
                            response.setFine(absentFine);
                            response.setAttendStatus(AttendStatus.ABSENT);
                        }
                    }
                }
                else{
                    throw new CustomException(ErrorCode.CHECK_NUMBER_MISMATCH);
                }
            }
            Attendance attendance = Attendance.builder()
                                              .userId(response.getUserId())
                                              .studySchedule(studySchedule.get())
                                              .attendStatus(response.getAttendStatus())
                                              .fine(response.getFine())
                                              .build();
            attendanceRepository.save(attendance);

            this.checkedExpire(attendance.getUserId(), attendance.getStudySchedule().getStudy());

            return response;
        }
        else{
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public int getCheckNumber(Long studyScheduleId) {
        Optional<StudySchedule> studySchedule = studyScheduleRepository.findById(studyScheduleId);
        if(studySchedule.isPresent()){
            return studySchedule.get().getCheckNumber();
        }
        else{
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public Map<String,Object> attendList(String userId, Long studyId, String searchDateStart, String searchDateEnd) {
        Map<String,Object> result = new HashMap<>();
        List<Map<String,Object>> detailList = new ArrayList<>();
        Optional<Study> study = studyRepository.findById(studyId);
        if(study.isPresent()){
            List<Integer> totalFine = new ArrayList<>();
            List<Integer> totalAttendance = new ArrayList<>();
            List<Integer> totalLateness = new ArrayList<>();
            List<Integer> totalAbsent = new ArrayList<>();
            List<Integer> totalHold = new ArrayList<>();

            study.get().getStudyScheduleList().forEach(x->{
                Map<String,Object> detail = new HashMap<>();
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
                if(searchDateStart.equals("")){
//                    if(now.isAfter(scheduleDateTimeEnd)){
//                        detail.put("studyScheduleDateTime", scheduleDateTimeStart);
//                    }
                    x.getAttendanceList().forEach(y->{
                        if(y.getUserId().equals(userId)){
                            if(now.isAfter(scheduleDateTimeEnd)){
                                detail.put("studyScheduleDateTime", scheduleDateTimeStart);
                            }
                            detail.put("fine", y.getFine());
                            detail.put("attendance", y.getAttendStatus());
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
                        }
                    });
                }
                else{
                    // 조회기간
                    LocalDate searchDateStartToLD = LocalDate.parse(searchDateStart);
                    LocalDate searchDateEndToLd = LocalDate.parse(searchDateEnd);
                    log.debug("조회 시작 날짜 : {}", searchDateStartToLD);
                    log.debug("조회 종료 날짜 : {}", searchDateEndToLd);
                    log.debug("스터디 날짜 : {}", x.getStudyScheduleDate());
                    if(searchDateStartToLD.isBefore(x.getStudyScheduleDate()) && (searchDateEndToLd.isAfter(x.getStudyScheduleDate()) || searchDateEndToLd.isEqual(x.getStudyScheduleDate()))){
                        //detail.put("studyScheduleDateTime", scheduleDateTimeStart);
                        x.getAttendanceList().forEach(y->{
                            if(y.getUserId().equals(userId)){
                                detail.put("studyScheduleDateTime", scheduleDateTimeStart);
                                detail.put("fine", y.getFine());
                                detail.put("attendance", y.getAttendStatus());
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
                            }
                        });
                    }
                }
                if(!detail.isEmpty()){
                    detailList.add(detail);
                }
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
            result.put("attendanceDetail", detailList);

            return result;
        }
        else{
            throw new CustomException(ErrorCode.STUDY_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public Map<String,Object> attendListForMaster(Long studyId, String searchDate) {
        Optional<Study> study = studyRepository.findById(studyId);
        Map<String, Object> result = new HashMap<>();

        if(study.isPresent()){
            LocalDate searchDateToLD = LocalDate.parse(searchDate);
            study.get().getStudyScheduleList().forEach(x->{
                if(searchDateToLD.isEqual(x.getStudyScheduleDate())){
                    List<Map<String,Object>> userAttendList = new ArrayList<>();
                    x.getAttendanceList().forEach(y->{
                        Map<String, Object> findUser = userservice.findUser(y.getUserId());
                        userAttendList.add(
                                Map.of(
                                        "userId", y.getUserId(),
                                        "nickName", findUser.get("nickname"),
                                        "img", findUser.get("img"),
                                        "attendance", y.getAttendStatus(),
                                        "fine", y.getFine(),
                                        "attendanceId", y.getAttendanceId()
                                )
                        );
                    });
                    result.put(x.getStudyScheduleStart(),userAttendList);
                }
            });
            return result;
        }
        else{
            throw new CustomException(ErrorCode.STUDY_NOT_FOUND);
        }
    }

    @Transactional
    public boolean updateAttend(Map<String, Object> param) {
        Optional<Attendance> isAttend = attendanceRepository.findById(Long.parseLong(param.get("attendanceId").toString()));
        if(isAttend.isPresent()){
            if(isAttend.get().getUserId().equals(param.get("userId").toString())){
                isAttend.get().updateStatusAndFine(param.get("attendStatus").toString(), Integer.parseInt(param.get("fine").toString()));
                alarmService.updateAttend(isAttend.get());
                return true;
            }
            else{
                throw new CustomException(ErrorCode.USER_MISMATCH_SCHEDULE);
            }
        }
        else {
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }

    @Transactional
    public void checkedExpire(String userId, Study study){
        Map<String,Object> studyRule = study.getStudyRule();

        // 강퇴 조건 - 결석
        HashMap out = (HashMap) studyRule.get("out");
        int absentExpireCnt = Integer.parseInt(out.get("absent").toString());
        int latenessExpireCnt = Integer.parseInt(out.get("lateness").toString());
        log.debug("지각 조건 : {}", latenessExpireCnt);
        log.debug("결석 조건 : {}", absentExpireCnt);
        if(absentExpireCnt > 0 || latenessExpireCnt > 0){
            List<Integer> late = new ArrayList<>();
            List<Integer> absent = new ArrayList<>();

            study.getStudyScheduleList().forEach(schedule->{
                schedule.getAttendanceList().forEach(attendance -> {
                    if(attendance.getUserId().equals(userId)){
                        if(attendance.getAttendStatus().equals(AttendStatus.LATENESS)){
                            late.add(1);
                        }
                        else if(attendance.getAttendStatus().equals(AttendStatus.ABSENT)){
                            absent.add(1);
                        }
                    }
                });
            });
            int latenessCnt = late.stream().mapToInt(Integer::intValue).sum();
            int absentCnt = absent.stream().mapToInt(Integer::intValue).sum();
            log.debug("지각 : {}", latenessCnt);
            log.debug("결석 : {}", absentCnt);
            if(latenessCnt >= latenessExpireCnt || absentCnt >= absentExpireCnt){
                alarmService.expireConfirm(userId,study);
            }
        }
    }

    public Map<String, Object> statistics(String userId, Long studyId) {
        Map<String,Object> result = new HashMap<>();
        Optional<Study> study = studyRepository.findById(studyId);
        if(study.isPresent()){
            List<Integer> totalFine = new ArrayList<>();
            List<Integer> totalAttendance = new ArrayList<>();
            List<Integer> totalLateness = new ArrayList<>();
            List<Integer> totalAbsent = new ArrayList<>();
            List<Integer> totalHold = new ArrayList<>();

            study.get().getStudyScheduleList().forEach(x->{
                x.getAttendanceList().forEach(y->{
                    if(y.getUserId().equals(userId)){
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
            result.put("totalAttendCnt", study.get().getStudyScheduleList().size());

            return result;
        }
        else{
            throw new CustomException(ErrorCode.STUDY_NOT_FOUND);
        }
    }

    public List<Map<String,Object>> statisticsForMaser(Long studyId) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<StudyMember> studyMemberList = studyMemberRepository.findByStudy_StudyId(studyId);
        studyMemberList.forEach(member->{
            Map<String, Object> getStatistics = this.statistics(member.getUser().getUserId(), studyId);
            getStatistics.put("userId", member.getUser().getUserId());
            getStatistics.put("img", ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/downloadIMG").queryParam("fileName", member.getUser().getImg()).toUriString());

            result.add(getStatistics);
        });
        return result;
    }
}

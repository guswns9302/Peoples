package com.peoples.api.service;

import com.peoples.api.domain.*;
import com.peoples.api.domain.enumeration.AttendStatus;
import com.peoples.api.domain.enumeration.Status;
import com.peoples.api.domain.security.SecurityUser;
import com.peoples.api.dto.request.UserScheduleRequest;
import com.peoples.api.dto.response.AttendanceResponse;
import com.peoples.api.dto.response.UserScheduleResponse;
import com.peoples.api.exception.CustomException;
import com.peoples.api.exception.ErrorCode;
import com.peoples.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.type.IntegerType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final StudyRepository studyRepository;
    private final StudyScheduleRepository studyScheduleRepository;
    private final AttendanceRepository attendanceRepository;
    private final AlarmService alarmService;

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
                    // ????????? ?????? ??? ?????? ?????? ??????
                    LocalDate scheduleDate = studySchedule.get().getStudyScheduleDate();
                    String startDateStr = scheduleDate.toString() + " " + studySchedule.get().getStudyScheduleStart();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime scheduleDateTimeStart = LocalDateTime.parse(startDateStr, formatter);

                    // ????????? ?????? ??? ?????? ?????? ??????
                    String endDateStr = scheduleDate.toString() + " " + studySchedule.get().getStudyScheduleEnd();
                    LocalDateTime scheduleDateTimeEnd = LocalDateTime.parse(endDateStr, formatter);

                    // ???????????? ?????? ????????????
                    LocalDateTime current = LocalDateTime.now();
                    LocalDateTime now = LocalDateTime.parse(current.format(formatter), formatter);

                    log.debug("?????? ?????? : {}", now);
                    log.debug("????????? ?????? : {}", scheduleDateTimeStart);
                    Map<String,Object> studyRule = studySchedule.get().getStudy().getStudyRule();
                    log.debug("????????? ?????? : {}", studyRule);
                    // ?????? ??????
                    HashMap lateness = (HashMap) studyRule.get("lateness");
                    int latenessTime = Integer.parseInt(lateness.get("time").toString());
                    int latenessFine = Integer.parseInt(lateness.get("fine").toString());
                    int latenessCnt = Integer.parseInt(lateness.get("count").toString());
                    // ?????? ??????
                    HashMap absent = (HashMap) studyRule.get("absent");
                    int absentTime = Integer.parseInt(absent.get("time").toString());
                    int absentFine = Integer.parseInt(absent.get("fine").toString());

                    if(latenessTime > 0 && absentTime > 0){
                        // ????????? ?????? ???
                        if(now.isBefore(scheduleDateTimeStart.plusMinutes(latenessTime)) && now.isEqual(scheduleDateTimeStart.plusMinutes(latenessTime))){
                            // ?????? ?????? ??? ??????
                            log.debug("?????? ?????? ??? ??????");
                            response.setFine(0);
                            response.setAttendStatus(AttendStatus.ATTENDANCE);
                        }
                        else if(now.isAfter(scheduleDateTimeStart.plusMinutes(latenessTime)) && now.isBefore(scheduleDateTimeStart.plusMinutes(absentTime + 1))){
                            // ?????? ?????? ??? ??????
                            log.debug("?????? ?????? ??? ??????");
                            Duration duration = Duration.between(scheduleDateTimeStart.plusMinutes(latenessTime), now);
                            // ?????? ??? ???
                            int cnt = (int) duration.getSeconds() / (latenessCnt * 60);
                            response.setFine(latenessFine * cnt);
                            response.setAttendStatus(AttendStatus.LATENESS);
                        }
                        else if(now.isAfter(scheduleDateTimeStart.plusMinutes(absentTime))){
                            // ?????? ?????? ??? ??????
                            log.debug("?????? ?????? ??? ??????");
                            response.setFine(absentFine);
                            response.setAttendStatus(AttendStatus.ABSENT);
                        }
                    }
                    else{
                        // ????????? ?????? ???
                        if(now.isBefore(scheduleDateTimeEnd)){
                            // ?????? ?????? ??? ??????
                            log.debug("?????? ?????? ??? ??????");
                            response.setFine(0);
                            response.setAttendStatus(AttendStatus.ATTENDANCE);
                        }
                        else if(now.isAfter(scheduleDateTimeEnd)){
                            // ?????? ?????? ??? ??????
                            log.debug("?????? ?????? ??? ??????");
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
    public int getCheckNumber(Map<String, Long> param) {
        Optional<StudySchedule> studySchedule = studyScheduleRepository.findById(param.get("studyScheduleId"));
        if(studySchedule.isPresent()){
            return studySchedule.get().getCheckNumber();
        }
        else{
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public Map<String,Object> attendList(String userId, Map<String, Object> param) {
        Map<String,Object> result = new HashMap<>();
        List<Map<String,Object>> detailList = new ArrayList<>();
        Optional<Study> study = studyRepository.findById(Long.parseLong(param.get("studyId").toString()));
        if(study.isPresent()){
            List<Integer> totalFine = new ArrayList<>();
            List<Integer> totalAttendance = new ArrayList<>();
            List<Integer> totalLateness = new ArrayList<>();
            List<Integer> totalAbsent = new ArrayList<>();
            List<Integer> totalHold = new ArrayList<>();

            study.get().getStudyScheduleList().forEach(x->{
                Map<String,Object> detail = new HashMap<>();
                // ????????? ?????? ??? ?????? ?????? ??????
                LocalDate scheduleDate = x.getStudyScheduleDate();
                String startDateStr = scheduleDate.toString() + " " + x.getStudyScheduleStart();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime scheduleDateTimeStart = LocalDateTime.parse(startDateStr, formatter);

                // ????????? ?????? ??? ?????? ?????? ??????
                String endDateStr = scheduleDate.toString() + " " + x.getStudyScheduleEnd();
                LocalDateTime scheduleDateTimeEnd = LocalDateTime.parse(endDateStr, formatter);

                // ????????????
                LocalDateTime current = LocalDateTime.now();
                LocalDateTime now = LocalDateTime.parse(current.format(formatter), formatter);
                if(param.get("searchDateStart").toString().equals("")){
                    if(now.isAfter(scheduleDateTimeEnd)){
                        detail.put("studyScheduleDateTime", scheduleDateTimeStart);
                    }
                    x.getAttendanceList().forEach(y->{
                        if(y.getUserId().equals(userId)){
                            detail.put("fine", y.getFine());
                            detail.put("attendStatus", y.getAttendStatus());
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
                    // ????????????
                    LocalDate searchDateStart = LocalDate.parse(param.get("searchDateStart").toString());
                    LocalDate searchDateEnd = LocalDate.parse(param.get("searchDateEnd").toString());
                    log.debug("?????? ?????? ?????? : {}", searchDateStart);
                    log.debug("?????? ?????? ?????? : {}", searchDateEnd);
                    log.debug("????????? ?????? : {}", x.getStudyScheduleDate());
                    if(searchDateStart.isBefore(x.getStudyScheduleDate()) && (searchDateEnd.isAfter(x.getStudyScheduleDate()) || searchDateEnd.isEqual(x.getStudyScheduleDate()))){
                        detail.put("studyScheduleDateTime", scheduleDateTimeStart);
                        x.getAttendanceList().forEach(y->{
                            if(y.getUserId().equals(userId)){
                                detail.put("fine", y.getFine());
                                detail.put("attendStatus", y.getAttendStatus());
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
    public Map<String,Object> attendListForMaster(Map<String, Object> param) {
        Optional<Study> study = studyRepository.findById(Long.parseLong(param.get("studyId").toString()));
        Map<String, Object> result = new HashMap<>();

        if(study.isPresent()){
            LocalDate searchDate = LocalDate.parse(param.get("searchDate").toString());
            study.get().getStudyScheduleList().forEach(x->{
                if(searchDate.isEqual(x.getStudyScheduleDate())){
                    List<Map<String,Object>> userAttendList = new ArrayList<>();
                    x.getAttendanceList().forEach(y->{
                        userAttendList.add(
                                Map.of(
                                        "userId", y.getUserId(),
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

        // ?????? ?????? - ??????
        HashMap out = (HashMap) studyRule.get("out");
        int absentExpireCnt = Integer.parseInt(out.get("absent").toString());
        int latenessExpireCnt = Integer.parseInt(out.get("lateness").toString());
        log.debug("?????? ?????? : {}", latenessExpireCnt);
        log.debug("?????? ?????? : {}", absentExpireCnt);
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
            log.debug("?????? : {}", latenessCnt);
            log.debug("?????? : {}", absentCnt);
            if(latenessCnt >= latenessExpireCnt || absentCnt >= absentExpireCnt){
                alarmService.expireConfirm(userId,study);
            }
        }
    }
}

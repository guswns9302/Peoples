package com.peoples.api.service;

import com.peoples.api.domain.Study;
import com.peoples.api.domain.StudyMember;
import com.peoples.api.domain.StudySchedule;
import com.peoples.api.domain.enumeration.RepeatType;
import com.peoples.api.dto.response.AttendanceResponse;
import com.peoples.api.dto.response.StudyScheduleResponse;
import com.peoples.api.exception.CustomException;
import com.peoples.api.exception.ErrorCode;
import com.peoples.api.repository.StudyRepository;
import com.peoples.api.repository.StudyScheduleRepository;
import com.peoples.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
public class StudyScheduleService {

    private final UserRepository userRepository;
    private final StudyRepository studyRepository;
    private final StudyScheduleRepository studyScheduleRepository;

    @Transactional(readOnly = true)
    public Map<Long,List<StudyScheduleResponse>> findSchedule(String userId) {
        List<StudyMember> studyMemberList = userRepository.findByUserId(userId).get().getStudyMemberList();
        Map<Long,List<StudyScheduleResponse>> joinStudyScheduleList = new HashMap<>();
        if(!studyMemberList.isEmpty()){
            studyMemberList.forEach(data->{
                List<StudyScheduleResponse> studyScheduleList = data.getStudy().getStudyScheduleList().stream().map(StudyScheduleResponse::from).collect(Collectors.toList());
                joinStudyScheduleList.put(data.getStudy().getStudyId(), studyScheduleList);
            });
        }
        return joinStudyScheduleList;
    }

    @Transactional
    public boolean createSchedule(Map<String, Object> param) {
        Optional<Study> study = studyRepository.findById(Long.parseLong(param.get("studyId").toString()));
        if(study.isPresent()){
            StudySchedule studySchedule = StudySchedule.builder()
                    .study(study.get())
                    .studyScheduleName(param.get("studyScheduleName").toString())
                    .studyScheduleDate(LocalDate.parse(param.get("studyScheduleDate").toString()))
                    .studySchedulePlace(param.get("studySchedulePlace").toString())
                    .checkNumber((int)(Math.random() * (9999 - 1000 + 1)) + 1000)
                    .studyScheduleStart(param.get("studyScheduleStart").toString())
                    .studyScheduleEnd(param.get("studyScheduleEnd").toString())
                    .build();

            studyScheduleRepository.save(studySchedule);
            studySchedule.repeatNumberIn(studySchedule.getStudyScheduleId());

            if(!(param.get("repeatDay").toString().equals(""))){
                LocalDate scheduleDate = LocalDate.parse(param.get("studyScheduleDate").toString());
                LocalDate targetDate = LocalDate.parse(param.get("targetDate").toString()).plusDays(1);
                if(param.get("repeatDay").toString().equals(RepeatType.everyDay.name())){
                    studySchedule.repeatTypeIn(RepeatType.everyDay.name());
                    LocalDate tomorrow = scheduleDate.plusDays(1);
                    while (tomorrow.isBefore(targetDate)){
                        StudySchedule repeatStudySchedule = StudySchedule.builder()
                                .study(study.get())
                                .studyScheduleName(param.get("studyScheduleName").toString())
                                .studyScheduleDate(tomorrow)
                                .studySchedulePlace(param.get("studySchedulePlace").toString())
                                .checkNumber((int)(Math.random() * (9999 - 1000 + 1)) + 1000)
                                .studyScheduleStart(param.get("studyScheduleStart").toString())
                                .studyScheduleEnd(param.get("studyScheduleEnd").toString())
                                .repeatNumber(studySchedule.getStudyScheduleId())
                                .repeatType(RepeatType.everyDay.name())
                                .build();
                        studyScheduleRepository.save(repeatStudySchedule);
                        tomorrow = tomorrow.plusDays(1);
                    }
                }
                else if(param.get("repeatDay").toString().equals(RepeatType.everyWeek.name())){
                    studySchedule.repeatTypeIn(RepeatType.everyWeek.name());
                    LocalDate week = scheduleDate.plusDays(7);
                    while (week.isBefore(targetDate)){
                        StudySchedule repeatStudySchedule = StudySchedule.builder()
                                .study(study.get())
                                .studyScheduleName(param.get("studyScheduleName").toString())
                                .studyScheduleDate(week)
                                .studySchedulePlace(param.get("studySchedulePlace").toString())
                                .checkNumber((int)(Math.random() * (9999 - 1000 + 1)) + 1000)
                                .studyScheduleStart(param.get("studyScheduleStart").toString())
                                .studyScheduleEnd(param.get("studyScheduleEnd").toString())
                                .repeatNumber(studySchedule.getStudyScheduleId())
                                .repeatType(RepeatType.everyWeek.name())
                                .build();
                        studyScheduleRepository.save(repeatStudySchedule);
                        week = week.plusDays(7);
                    }
                }
                else if(param.get("repeatDay").toString().equals(RepeatType.everyTwoWeek.name())){
                    studySchedule.repeatTypeIn(RepeatType.everyTwoWeek.name());
                    LocalDate twoweek = scheduleDate.plusDays(14);
                    while (twoweek.isBefore(targetDate)){
                        StudySchedule repeatStudySchedule = StudySchedule.builder()
                                .study(study.get())
                                .studyScheduleName(param.get("studyScheduleName").toString())
                                .studyScheduleDate(twoweek)
                                .studySchedulePlace(param.get("studySchedulePlace").toString())
                                .checkNumber((int)(Math.random() * (9999 - 1000 + 1)) + 1000)
                                .studyScheduleStart(param.get("studyScheduleStart").toString())
                                .studyScheduleEnd(param.get("studyScheduleEnd").toString())
                                .repeatNumber(studySchedule.getStudyScheduleId())
                                .repeatType(RepeatType.everyTwoWeek.name())
                                .build();
                        studyScheduleRepository.save(repeatStudySchedule);
                        twoweek = twoweek.plusDays(7);
                    }
                }
                else if(param.get("repeatDay").toString().equals(RepeatType.everyMonth.name())){
                    studySchedule.repeatTypeIn(RepeatType.everyMonth.name());
                    LocalDate month = scheduleDate.plusMonths(1);
                    while (month.isBefore(targetDate)){
                        StudySchedule repeatStudySchedule = StudySchedule.builder()
                                .study(study.get())
                                .studyScheduleName(param.get("studyScheduleName").toString())
                                .studyScheduleDate(month)
                                .studySchedulePlace(param.get("studySchedulePlace").toString())
                                .checkNumber((int)(Math.random() * (9999 - 1000 + 1)) + 1000)
                                .studyScheduleStart(param.get("studyScheduleStart").toString())
                                .studyScheduleEnd(param.get("studyScheduleEnd").toString())
                                .repeatNumber(studySchedule.getStudyScheduleId())
                                .repeatType(RepeatType.everyMonth.name())
                                .build();
                        studyScheduleRepository.save(repeatStudySchedule);
                        month = month.plusMonths(1);
                    }
                }
            }
            return true;
        }
        else{
            throw new CustomException(ErrorCode.STUDY_NOT_FOUND);
        }
    }

    @Transactional
    public boolean updateSchedule(Map<String, Object> param) {
        Optional<StudySchedule> studySchedule = studyScheduleRepository.findById(Long.parseLong(param.get("studyScheduleId").toString()));
        if(studySchedule.isPresent()){
            if(Boolean.parseBoolean(param.get("isRepeat").toString())){
                //반복일정도 같이 변경 (현재 시점 기준 이후 일정만)
                Long repeatNum = studySchedule.get().getRepeatNumber();
                List<StudySchedule> studyScheduleList = studyScheduleRepository.findByRepeatNumber(repeatNum);
                studyScheduleList.forEach(list->{
                    // 스터디 날짜 및 시작 시간 조합
                    LocalDate scheduleDate = list.getStudyScheduleDate();
                    String startDateStr = scheduleDate.toString() + " " + list.getStudyScheduleStart();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime scheduleDateTimeStart = LocalDateTime.parse(startDateStr, formatter);

                    // 현재시간
                    LocalDateTime current = LocalDateTime.now();
                    LocalDateTime now = LocalDateTime.parse(current.format(formatter), formatter);
                    if(scheduleDateTimeStart.isAfter(now)){
                        // 일정이 현재 날짜보다 이후인 경우에 삭제
                        studyScheduleRepository.delete(list);
                    }
                    // 새로운 반복 일정으로 생성
                });
                return this.createSchedule(param);
            }
            else{
                //현재 일정만 변경
                studySchedule.get().updateStudySchedule(param);
                return true;
            }
        }
        else{
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }

    @Transactional
    public boolean delete(Map<String,Object> param) {
        Optional<StudySchedule> studySchedule = studyScheduleRepository.findById(Long.parseLong(param.get("studyScheduleId").toString()));
        if(studySchedule.isPresent()){

            if(Boolean.parseBoolean(param.get("repeatDelete").toString())){
                List<StudySchedule> byRepeatNumber = studyScheduleRepository.findByRepeatNumber(studySchedule.get().getRepeatNumber());
                byRepeatNumber.forEach(list->{
                    studyScheduleRepository.delete(list);
                });
            }
            else{
                studyScheduleRepository.delete(studySchedule.get());
            }
            return true;
        }
        else{
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }

    @Transactional
    public AttendanceResponse getAttendByStudyScheduleId(Long studyScheduleId, String userId) {
        Optional<StudySchedule> studySchedule = studyScheduleRepository.findById(studyScheduleId);
        AttendanceResponse attendanceResponse = new AttendanceResponse();
        if(studySchedule.isPresent()){
            studySchedule.get().getAttendanceList().forEach(attend->{
                if(attend.getUserId().equals(userId)){
                    attendanceResponse.setUserId(attend.getUserId());
                    attendanceResponse.setAttendStatus(attend.getAttendStatus());
                    attendanceResponse.setReason(attend.getReason());
                    attendanceResponse.setFine(attend.getFine());
                }
            });
            return attendanceResponse;
        }
        else{
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }
}

package com.peoples.api.scheduler;

import com.peoples.api.domain.Attendance;
import com.peoples.api.domain.StudySchedule;
import com.peoples.api.domain.enumeration.AttendStatus;
import com.peoples.api.repository.AttendanceRepository;
import com.peoples.api.repository.StudyScheduleRepository;
import com.peoples.api.service.AlarmService;
import com.peoples.api.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class StaticScheduler {

    private final StudyScheduleRepository studyScheduleRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceService attendanceService;

    @Transactional
    @Scheduled(cron = "0 */1 * * * *")	// 1분마다
    public void scheduleTaskUsingCronExpression() {

        // 현재시간
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime targetTime = LocalDateTime.parse(current.format(formatter), formatter);

        List<StudySchedule> byStudyScheduleDate = studyScheduleRepository.findByStudyScheduleDate(targetTime.toLocalDate());
        byStudyScheduleDate.forEach(x->{
            // 스터디 날짜 및 종료 시간 조합
            LocalDate scheduleDate = x.getStudyScheduleDate();
            String endDateStr = scheduleDate.toString() + " " + x.getStudyScheduleEnd();
            LocalDateTime scheduleDateTimeEnd = LocalDateTime.parse(endDateStr, formatter);

            Map<String,Object> studyRule = x.getStudy().getStudyRule();
            // 결석 시간
            HashMap absent = (HashMap) studyRule.get("absent");
            int absentFine = Integer.parseInt(absent.get("fine").toString());

            if(targetTime.isAfter(scheduleDateTimeEnd)){
                log.debug("현재 시간 : {}", targetTime);
                log.debug("스터디 종료 시간 : {}", scheduleDateTimeEnd);
                if(x.getAttendanceList().size() == x.getStudy().getStudyMemberList().size()){
                    log.debug("전원 출석 하였습니다.");
                }
                else{
                    List<String> studyMember = new ArrayList<>();
                    List<String> attendMember = new ArrayList<>();

                    x.getStudy().getStudyMemberList().forEach(sm->{
                        studyMember.add(sm.getUser().getUserId());
                    });
                    x.getAttendanceList().forEach(am->{
                        attendMember.add(am.getUserId());
                    });

                    List<String> diff = studyMember.stream()
                            .filter(i -> !attendMember.contains(i))
                            .collect(Collectors.toList());
                    diff.forEach(who -> {
                        Attendance attendance = Attendance.builder()
                                .userId(who)
                                .studySchedule(x)
                                .attendStatus(AttendStatus.ABSENT)
                                .fine(absentFine)
                                .build();
                        attendanceRepository.save(attendance);
                        attendanceService.checkedExpire(who, x.getStudy());
                    });
                }
            }
        });
    }

    @Scheduled(cron = "0 */1 * * * *")	// 1분마다
    public void sendPushMsg(){
        log.debug("pushMSG를 보내야 하는데.....");
    }
}

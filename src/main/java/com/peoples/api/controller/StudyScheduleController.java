package com.peoples.api.controller;

import com.peoples.api.domain.StudySchedule;
import com.peoples.api.domain.security.SecurityUser;
import com.peoples.api.dto.response.AttendanceResponse;
import com.peoples.api.dto.response.StudyScheduleResponse;
import com.peoples.api.service.StudyScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class StudyScheduleController {
    private final StudyScheduleService studyScheduleService;

    @GetMapping("/study/schedule")
    public ResponseEntity<Map<Long,List<StudyScheduleResponse>>> findStudySchedule(@AuthenticationPrincipal SecurityUser user){
        return ResponseEntity.ok(studyScheduleService.findSchedule(user.getUsername()));
    }

    @PostMapping("/study/schedule")
    public ResponseEntity<Boolean> createStudySchedule(@RequestBody Map<String,Object> param){
        return ResponseEntity.ok(studyScheduleService.createSchedule(param));
    }

    @PutMapping("/study/schedule")
    public ResponseEntity<Boolean> updateStudySchedule(@RequestBody Map<String,Object> param){
        return ResponseEntity.ok(studyScheduleService.updateSchedule(param));
    }

    @DeleteMapping("study/schedule")
    public ResponseEntity<Boolean> deleteStudySchedule(@RequestBody Map<String,Object> param){
        return ResponseEntity.ok(studyScheduleService.delete(param));
    }

    @GetMapping("/study/schedule/{studyScheduleId}")
    public ResponseEntity<AttendanceResponse> getStudyScheduleAttend (@AuthenticationPrincipal SecurityUser user, @PathVariable Long studyScheduleId){
        log.debug("user name : {}", user.getUsername());
        log.debug("studyScheduleId : {}", studyScheduleId);
        return ResponseEntity.ok(studyScheduleService.getAttendByStudyScheduleId(studyScheduleId, user.getUsername()));
    }
}

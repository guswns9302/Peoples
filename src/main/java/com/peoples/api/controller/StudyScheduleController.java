package com.peoples.api.controller;

import com.peoples.api.domain.security.SecurityUser;
import com.peoples.api.service.StudyScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class StudyScheduleController {
    private final StudyScheduleService studyScheduleService;

    @GetMapping("/study/schedule")
    public ResponseEntity<Map<String,Object>> findUserSchedule(@AuthenticationPrincipal SecurityUser user){
        return ResponseEntity.ok(studyScheduleService.findSchedule(user.getUsername()));
    }
}

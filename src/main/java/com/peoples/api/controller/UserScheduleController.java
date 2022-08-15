package com.peoples.api.controller;

import com.peoples.api.domain.security.SecurityUser;
import com.peoples.api.dto.request.UserScheduleRequest;
import com.peoples.api.service.UserScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class UserScheduleController {

    private final UserScheduleService userScheduleService;

    @GetMapping("/user/schedule")
    public ResponseEntity<Map<String,Object>> findUserSchedule(@AuthenticationPrincipal SecurityUser user){
        return ResponseEntity.ok(userScheduleService.findUserSchedule(user.getUsername()));
    }

    @PostMapping("/user/schedule")
    public ResponseEntity<Map<String,Object>> createUserSchedule(@RequestBody UserScheduleRequest userScheduleRequest, @AuthenticationPrincipal SecurityUser user){
        return ResponseEntity.ok(userScheduleService.createUserSchedule(userScheduleRequest,user));
    }

    @PutMapping("/user/schedule/{scheduleId}")
    public ResponseEntity<Map<String,Object>> updateStatus(@PathVariable long scheduleId){
        return ResponseEntity.ok(userScheduleService.updateStatus(scheduleId));
    }

    @PutMapping("/user/schedule")
    public ResponseEntity<Map<String,Object>> updateSchedule(@RequestBody Map<String,Object> param){
        return ResponseEntity.ok(userScheduleService.updateSchedule(param));
    }
}

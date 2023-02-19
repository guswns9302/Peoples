package com.peoples.api.controller;

import com.peoples.api.domain.security.SecurityUser;
import com.peoples.api.dto.response.AlarmResponse;
import com.peoples.api.dto.response.AttendanceResponse;
import com.peoples.api.service.AlarmService;
import com.peoples.api.service.AttendanceService;
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
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping("/alarm")
    public ResponseEntity<List<AlarmResponse>> getAlarmList(@AuthenticationPrincipal SecurityUser user){
        return ResponseEntity.ok(alarmService.getAlarmList(user.getUsername()));
    }

}

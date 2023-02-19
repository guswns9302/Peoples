package com.peoples.api.controller;

import com.peoples.api.domain.security.SecurityUser;
import com.peoples.api.dto.response.AttendanceResponse;
import com.peoples.api.dto.response.DeleteUserResponse;
import com.peoples.api.dto.response.UserStudyHistoryResponse;
import com.peoples.api.service.AttendanceService;
import com.peoples.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/attendance")
    public ResponseEntity<AttendanceResponse> attend(@AuthenticationPrincipal SecurityUser user, @RequestBody Map<String, Object> param){
        return ResponseEntity.ok(attendanceService.attendSchedule(user.getUsername(), param));
    }

    @GetMapping("/attendance/checkNumber/{studyScheduleId}")
    public ResponseEntity<Integer> attend(@PathVariable Long studyScheduleId){
        return ResponseEntity.ok(attendanceService.getCheckNumber(studyScheduleId));
    }

    @GetMapping("/attendance/{userId}")
    public ResponseEntity<Map<String,Object>> attendList(@PathVariable String userId, @RequestParam Long studyId, @RequestParam String searchDateStart, @RequestParam String searchDateEnd){
        return ResponseEntity.ok(attendanceService.attendList(userId, studyId, searchDateStart, searchDateEnd));
    }

    @GetMapping("/attendance/master/{studyId}")
    public ResponseEntity<Map<String,Object>> attendListForMaster(@PathVariable Long studyId, @RequestParam String searchDate){
        return ResponseEntity.ok(attendanceService.attendListForMaster(studyId, searchDate));
    }

    @PutMapping("/attendance/master")
    public ResponseEntity<Boolean> updateAttend(@RequestBody Map<String, Object> param){
        return ResponseEntity.ok(attendanceService.updateAttend(param));
    }
}

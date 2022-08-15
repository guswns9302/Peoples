package com.peoples.api.controller;

import com.peoples.api.domain.security.SecurityUser;
import com.peoples.api.dto.request.StudyRequest;
import com.peoples.api.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StudyController {

    private final StudyService studyService;

    @PostMapping("/study")
    public ResponseEntity<Map<String,Object>> create(@RequestBody StudyRequest param, @AuthenticationPrincipal SecurityUser user){
        return ResponseEntity.ok(studyService.create(param, user));
    }

    // 스터디 정보 조회
    @GetMapping("/study/{studyId}")
    public ResponseEntity<Map<String,Object>> findStudy(@PathVariable Long studyId){
        return ResponseEntity.ok(studyService.findStudy(studyId));
    }

    // 내가 참여하고 있는 모든 스터디
    @GetMapping("/study")
    public ResponseEntity<Map<String,Object>> findAllStudy(@AuthenticationPrincipal SecurityUser user){
        return ResponseEntity.ok(studyService.findAll(user.getUsername()));
    }
}

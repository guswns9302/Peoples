package com.peoples.api.controller;

import com.peoples.api.domain.security.SecurityUser;
import com.peoples.api.dto.request.StudyNotiRequest;
import com.peoples.api.dto.request.StudyRequest;
import com.peoples.api.dto.response.ParticipationStudyResponse;
import com.peoples.api.dto.response.StudyNotiAllResponse;
import com.peoples.api.dto.response.StudyResponse;
import com.peoples.api.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StudyController {

    private final StudyService studyService;

    @PostMapping("/study")
    public ResponseEntity<StudyResponse> create(@RequestBody StudyRequest param, @AuthenticationPrincipal SecurityUser user){
        return ResponseEntity.ok(studyService.create(param, user));
    }

    @PutMapping("/study/{studyId}")
    public ResponseEntity<StudyResponse> updateStudy(@PathVariable long studyId, @RequestBody StudyRequest param){
        return ResponseEntity.ok(studyService.updateStudy(studyId,param));
    }

    @PutMapping("/study/end/{studyId}")
    public ResponseEntity<Boolean> finishStudy(@PathVariable long studyId){
        return ResponseEntity.ok(studyService.finishStudy(studyId));
    }

    @PostMapping("/study/join/{studyId}")
    public ResponseEntity<Boolean> joinStudy(@PathVariable long studyId, @AuthenticationPrincipal SecurityUser user){
        return ResponseEntity.ok(studyService.join(studyId, user.getUser()));
    }

    // 스터디 정보 조회
    @GetMapping("/study/{studyId}")
    public ResponseEntity<Map<String,Object>> findStudy(@PathVariable Long studyId, @AuthenticationPrincipal SecurityUser user){
        return ResponseEntity.ok(studyService.findStudy(studyId, user));
    }

    // 참여한 스터디 ( 림스 요청 )
    @GetMapping("/study/participation")
    public ResponseEntity<List<ParticipationStudyResponse>> participationStudyList(@AuthenticationPrincipal SecurityUser user){
        return ResponseEntity.ok(studyService.findParticipationStudyList(user.getUsername()));
    }

    // 내가 참여하고 있는 모든 스터디
    @GetMapping("/study")
    public ResponseEntity<List<StudyResponse>> findAllStudy(@AuthenticationPrincipal SecurityUser user){
        return ResponseEntity.ok(studyService.findAll(user.getUsername()));
    }

    @PostMapping("/noti")
    public ResponseEntity<List<StudyNotiAllResponse>> createNoti(@RequestBody StudyNotiRequest studyNotiRequest){
        return ResponseEntity.ok(studyService.createStudyNoti(studyNotiRequest));
    }

    @GetMapping("/noti/{studyId}")
    public ResponseEntity<List<StudyNotiAllResponse>> getAllNoti(@PathVariable Long studyId){
        return ResponseEntity.ok(studyService.findStudyNoti(studyId));
    }

    @PutMapping("/noti")
    public ResponseEntity<List<StudyNotiAllResponse>> updateNoti(@RequestBody Map<String, Object> param){
        return ResponseEntity.ok(studyService.updateStudyNoti(param));
    }

    @PutMapping("/noti/pin")
    public ResponseEntity<List<StudyNotiAllResponse>> updatePin(@RequestBody Map<String, Object> param){
        return ResponseEntity.ok(studyService.updatePin(param));
    }

    @PutMapping("/noti/pin/compulsion/{notificationId}")
    public ResponseEntity<List<StudyNotiAllResponse>> updatePinCompulsion(@PathVariable Long notificationId){
        return ResponseEntity.ok(studyService.updatePinCompulsion(notificationId));
    }

    @DeleteMapping("/noti/{notificationId}")
    public ResponseEntity<List<StudyNotiAllResponse>> deleteNoti(@PathVariable Long notificationId){
        return ResponseEntity.ok(studyService.deleteStudyNoti(notificationId));
    }
}

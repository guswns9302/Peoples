package com.peoples.api.controller;

import com.peoples.api.domain.StudyMember;
import com.peoples.api.domain.security.SecurityUser;
import com.peoples.api.dto.request.StudyNotiRequest;
import com.peoples.api.dto.request.StudyRequest;
import com.peoples.api.dto.response.StudyMemberResponse;
import com.peoples.api.dto.response.StudyNotiAllResponse;
import com.peoples.api.dto.response.StudyResponse;
import com.peoples.api.service.StudyMemberService;
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
public class StudyMemberController {

    private final StudyMemberService studyMemberService;

    @GetMapping("/studyMember/{studyId}")
    public ResponseEntity<Map<String,Object>> getMemberList(@PathVariable long studyId, @AuthenticationPrincipal SecurityUser user){
        return ResponseEntity.ok(studyMemberService.getList(studyId, user.getUsername()));
    }

    @PutMapping("/studyMember/memberRole")
    public ResponseEntity<Boolean> updateMemberRole(@RequestBody Map<String,Object> param){
        return ResponseEntity.ok(studyMemberService.updateRole(param));
    }

    @PutMapping("/studyMember/manager")
    public ResponseEntity<Boolean> updateManager(@RequestBody Map<String,Object> param){
        return ResponseEntity.ok(studyMemberService.updateManager(param));
    }

    @DeleteMapping("/studyMember/{studyMemberId}")
    public ResponseEntity<Boolean> expire(@PathVariable long studyMemberId, @AuthenticationPrincipal SecurityUser user){
        return ResponseEntity.ok(studyMemberService.expire(studyMemberId, user.getUser()));
    }

    @DeleteMapping("/studyMember/leave/{studyId}")
    public ResponseEntity<Boolean> leave(@PathVariable long studyId, @AuthenticationPrincipal SecurityUser user){
        return ResponseEntity.ok(studyMemberService.leave(studyId, user.getUsername()));
    }

    @PutMapping("/studyMember/master")
    public ResponseEntity<Boolean> changeMaster(@AuthenticationPrincipal SecurityUser user, @RequestBody Map<String, Long> param){
        return ResponseEntity.ok(studyMemberService.changeMaster(user.getUser(),param));
    }
}

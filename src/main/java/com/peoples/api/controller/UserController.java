package com.peoples.api.controller;

import com.peoples.api.domain.security.SecurityUser;
import com.peoples.api.service.EmailService;
import com.peoples.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    // 회원가입 -> email 중복 검증
    @PostMapping("/signup/verification")
    public ResponseEntity<Map<String,Object>> emailVerification(@RequestBody Map<String, Object> param){
        return ResponseEntity.ok(userService.verificationEmail(param));
    }

    // 회원가입
    @PostMapping(value = "/signup" , consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Map<String,Object>> createUser(@RequestPart Map<String, Object> param, @RequestPart MultipartFile file){
        return ResponseEntity.ok(userService.createUser(param, file));
    }

    // JwtAuthenticationProcessingFilter 에서 refreshToken 으로 검증 후 재발행
    @PostMapping("/issued")
    public void reIssuedToken() {}

    // 패스워드 찾기
    @GetMapping("/user/password")
    public ResponseEntity sendUserPassword(@RequestParam String userId){
        log.debug("userId : {}", userId);
        return null;
    }

    // 프로필 이미지 변경
    @PutMapping(value = "/user/profile/img", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Map<String,Object>> uploadImg(@RequestPart Map<String, Object> param, @RequestPart MultipartFile file){
        return ResponseEntity.ok(userService.profileChange(param,file));
    }

    // 회원가입 이메일 인증
    @PostMapping("/email/{userId}")
    public ResponseEntity<Map<String,Object>> emailAuth(@PathVariable String userId){
        return ResponseEntity.ok(emailService.sendEmail(userId));
    }

    // 회원 탈퇴
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Map<String,Object>> deleteUser(@PathVariable String userId){
        return ResponseEntity.ok(userService.deleteUser(userId));
    }

    // 회원 닉네임 변경
    @PutMapping("/user/nickname")
    public ResponseEntity<Map<String,Object>> changeNickname(@AuthenticationPrincipal SecurityUser user, @RequestBody Map<String,Object> param){
        return ResponseEntity.ok(userService.changeNickname(user.getUsername(), param));
    }

    // 마이페이지 - 스터디 히스토리 (내가 참여한 스터디)
    @GetMapping("/user/history")
    public ResponseEntity<Map<String,Object>> history(@AuthenticationPrincipal SecurityUser user){
        return ResponseEntity.ok(userService.history(user.getUsername()));
    }
}

package com.peoples.api.controller;

import com.peoples.api.domain.security.SecurityUser;
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
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

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

    // 회원가입 이메일 인증 메일 재발송
    @GetMapping("/user/email/auth")
    public ResponseEntity<Map<String,Object>> reSendAuthMail(@AuthenticationPrincipal SecurityUser user){
        return ResponseEntity.ok(userService.reSendAuthMail(user.getUser()));
    }

    // JwtAuthenticationProcessingFilter 에서 refreshToken 으로 검증 후 재발행
    @PostMapping("/issued")
    public void reIssuedToken() {}

    // 패스워드 찾기 --> 임시 비밀번호 전송
    @GetMapping("/user/password")
    public ResponseEntity<Map<String,Object>> sendUserPassword(@RequestParam String userId){
        return ResponseEntity.ok(userService.tempPassword(userId));
    }

    // 프로필 이미지 다운로드
    @GetMapping("/downloadIMG")
    public ResponseEntity<Resource> downloadIMG(@RequestParam String fileName) throws IOException {
        Path path = Paths.get(System.getProperty("user.home") + "/profile/" + fileName);
        String contentType = Files.probeContentType(path);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);

        Resource resource = new InputStreamResource(Files.newInputStream(path));
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);

    }

    // 회원 탈퇴
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Map<String,Object>> deleteUser(@PathVariable String userId){
        return ResponseEntity.ok(userService.deleteUser(userId));
    }

    // 마이페이지 - 스터디 히스토리 (내가 참여한 스터디)
    @GetMapping("/user/history")
    public ResponseEntity<Map<String,Object>> history(@AuthenticationPrincipal SecurityUser user){
        return ResponseEntity.ok(userService.history(user.getUsername()));
    }

    // 회원 정보 변경
    @PutMapping(value = "/user", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Map<String,Object>> userUpdate(@AuthenticationPrincipal SecurityUser user, @RequestPart Map<String, Object> param, @RequestPart MultipartFile file){
        return ResponseEntity.ok(userService.updateUser(user.getUser().getUserId(),param, file));
    }
}

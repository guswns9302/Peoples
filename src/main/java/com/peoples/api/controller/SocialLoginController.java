package com.peoples.api.controller;

import com.peoples.api.dto.response.SocialLoginResponse;
import com.peoples.api.dto.response.UserResponse;
import com.peoples.api.service.SocialLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class SocialLoginController {
    private final SocialLoginService socialLoginService;

    @GetMapping("/login/oauth/{provider}")
    public ResponseEntity<Map<String,Object>> socialLogin(@PathVariable String provider ,@RequestParam String code){
        log.debug("provider : {}", provider);
        log.debug("code : {}", code);
        SocialLoginResponse result = socialLoginService.login(provider, code);
        HttpHeaders headers = new HttpHeaders();
        headers.add("AccessToken", result.getAccessToken());
        headers.add("RefreshToken", result.getRefreshToken());

        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put("timestamp", LocalDateTime.now());
        responseMap.put("message", "Social login success. Issued AccessToken , RefreshToken");
        responseMap.put("result", UserResponse.from(result.getUser()));

        return ResponseEntity.ok().headers(headers).body(responseMap);
    }

    @GetMapping("/login/oauth2/{provider}")
    public ResponseEntity<Map<String,Object>> oauth2Login(@PathVariable String provider, @RequestParam String token){
        log.debug("provider : {}", provider);
        log.debug("token : {}", token);

        SocialLoginResponse result = socialLoginService.oauth2Login(provider, token);

        HttpHeaders headers = new HttpHeaders();
        headers.add("AccessToken", result.getAccessToken());
        headers.add("RefreshToken", result.getRefreshToken());

        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put("timestamp", LocalDateTime.now());
        responseMap.put("message", "Social login success. Issued AccessToken , RefreshToken");
        responseMap.put("result", UserResponse.from(result.getUser()));

        return ResponseEntity.ok().headers(headers).body(responseMap);
    }

}

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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class SocialLoginController {
    private final SocialLoginService socialLoginService;

    private ResponseEntity<UserResponse> oauth2Response(SocialLoginResponse result){
        HttpHeaders headers = new HttpHeaders();
        headers.add("AccessToken", result.getAccessToken());
        headers.add("RefreshToken", result.getRefreshToken());
        return ResponseEntity.ok().headers(headers).body(UserResponse.of(result.getUser()));
    }

    @GetMapping("/login/oauth/{provider}")
    public ResponseEntity<UserResponse> restApiOauth2Login(@PathVariable String provider ,@RequestParam String code){
        log.debug("provider : {}", provider);
        log.debug("code : {}", code);
        SocialLoginResponse result = socialLoginService.restApiLogin(provider, code);
        return this.oauth2Response(result);
    }

    @GetMapping("/login/oauth2/{provider}")
    public ResponseEntity<UserResponse> oauth2Login(@PathVariable String provider, @RequestParam String token){
        log.debug("provider : {}", provider);
        log.debug("token : {}", token);
        SocialLoginResponse result = socialLoginService.oauth2Login(provider, token);
        return this.oauth2Response(result);
    }

}

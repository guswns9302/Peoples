package com.peoples.api.controller;

import com.peoples.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EmailRedirectController {
    private final UserService userService;

    // 회원가입 이메일 인증
    @GetMapping("/email/auth")
    public String emailAuth(@RequestParam String token){
        boolean result = userService.emailAuth(token);

        if(result == true){
            return "emailAuthSuccess";
        }
        else{
            return "emailAuthFail";
        }
    }
}

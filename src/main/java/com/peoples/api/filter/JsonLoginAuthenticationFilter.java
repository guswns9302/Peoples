package com.peoples.api.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JsonLoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String DEFAULT_LOGIN_REQUEST_URL = "/api/v1/signin";

    private static final String HTTP_METHOD = "POST";    //HTTP 메서드의 방식은 POST 이다.

    private static final String CONTENT_TYPE = "application/json";//json 타입의 데이터로만 로그인을 진행한다.

    private final ObjectMapper objectMapper;

    private static final String USERID_KEY = "userId";
    private static final String PASSWORD_KEY = "password";


    private static final AntPathRequestMatcher DEFAULT_LOGIN_PATH_REQUEST_MATCHER = new AntPathRequestMatcher(DEFAULT_LOGIN_REQUEST_URL, HTTP_METHOD); //POST로 온 로그인 요청에 매칭된다.

    public JsonLoginAuthenticationFilter(ObjectMapper objectMapper) {
        super(DEFAULT_LOGIN_PATH_REQUEST_MATCHER);   // 위에서 설정한  /oauth2/login/* 의 요청에, GET으로 온 요청을 처리하기 위해 설정한다.
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(@NotNull HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        System.out.println("JsonLoginAuthenticationFilter 실행");

        System.out.println(request.getContentType());
        if(request.getContentType() == null || !request.getContentType().equals(CONTENT_TYPE)  ) {
            throw new AuthenticationServiceException("Authentication Content-Type not supported: " + request.getContentType());
        }
        String messageBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        Map<String, String> userIdPasswordMap = objectMapper.readValue(messageBody, Map.class);
        String userId = userIdPasswordMap.get(USERID_KEY);
        String password = userIdPasswordMap.get(PASSWORD_KEY);
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(userId, password);//principal 과 credentials 전달
        Authentication authenticate = this.getAuthenticationManager().authenticate(authRequest);
        return authenticate;
    }
}

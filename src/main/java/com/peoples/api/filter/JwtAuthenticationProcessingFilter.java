package com.peoples.api.filter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.peoples.api.config.GsonConfig;
import com.peoples.api.domain.User;
import com.peoples.api.domain.security.SecurityUser;
import com.peoples.api.exception.ErrorCode;
import com.peoples.api.exception.ErrorResponse;
import com.peoples.api.repository.UserRepository;
import com.peoples.api.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String NO_CHECK_URL_SIGNIN = "/api/v1/signin";
        String NO_CHECK_URL_SIGNUP = "/api/v1/signup";
        String NO_CHECK_URL_SIGNUP_VERIFICATION = "/api/v1/signup/verification";
        String NO_CHECK_URL_REISSUED = "/api/v1/issued";
        String NO_CHECK_URL_PASSWORD = "/api/v1/user/password";
        String NO_CHECK_URL_APIDOCS = "/v3/api-docs";
        String NO_CHECK_URL_SWAGGER = "/swagger-ui.html";
        String NO_CHECK_URL_SWAGGER_ANY = "/swagger-ui/";
        if(request.getRequestURI().equals(NO_CHECK_URL_SIGNIN) || request.getRequestURI().equals(NO_CHECK_URL_SIGNUP)
            || request.getRequestURI().equals(NO_CHECK_URL_SIGNUP_VERIFICATION) || request.getRequestURI().equals(NO_CHECK_URL_PASSWORD)
            || request.getRequestURI().contains(NO_CHECK_URL_APIDOCS) || request.getRequestURI().contains(NO_CHECK_URL_SWAGGER)
            || request.getRequestURI().contains(NO_CHECK_URL_SWAGGER_ANY)) {
            filterChain.doFilter(request, response);
            return;
        }
        else if(request.getRequestURI().equals(NO_CHECK_URL_REISSUED)){
            this.tokenREISSUED(request, response);
            return;
        }

        // 엑세스 토큰과 리프레쉬 토큰 추출 하고 유효한 토큰인지 확인.
        String refreshToken = jwtService.extractRefreshToken(request).filter(jwtService::isTokenValid).orElse(null);
        String accessToken = jwtService.extractAccessToken(request).filter(jwtService::isTokenValid).orElse(null);
        log.debug("두필터 실행");
        if(accessToken != null && refreshToken != null){
            log.debug("엑세스 인증");
            checkAccessTokenAndAuthentication(request, response, filterChain, accessToken);
        }
        else if((accessToken == null && refreshToken == null) || (accessToken != null && refreshToken == null)){
            log.debug("둘다 실패");
            this.reLogin(response);
        }
        else if(accessToken == null && refreshToken != null){
            log.debug("리프레쉬 인증");
            checkRefreshToken(response, refreshToken);
        }
    }
    // 재 로그인 요청
    private void reLogin(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); //401 -> 다시 로그인
        this.createJsonForErrorResponse(response, ErrorCode.UNAUTHORIZED_USER);
    }

    // access token 인증
    private void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, String accessToken) throws ServletException, IOException {
        Optional<String> userIdFromJWT = jwtService.extractUserId(accessToken);
        if(userIdFromJWT.isPresent()){
            Optional<User> user = userRepository.findByUserId(userIdFromJWT.get());
            if(user.isPresent()){
                this.saveAuthentication(user.get());
                filterChain.doFilter(request,response);
            }
            else{
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
                this.createJsonForErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR);
            }
        }
        else{
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            this.createJsonForErrorResponse(response, ErrorCode.UNAUTHORIZED_USER);
        }
    }
    // access token 인증 후 사용자 정보 맵핑
    private void saveAuthentication(User user) {
        UserDetails userDetails = SecurityUser.of(User.builder()
                                                        .userId(user.getUserId())
                                                        .password(user.getPassword())
                                                        .nickname(user.getNickname())
                                                        .role(user.getRole())
                                                        .build());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,authoritiesMapper.mapAuthorities(userDetails.getAuthorities()));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    //refresh token 인증 절차
    private void checkRefreshToken(HttpServletResponse response, String refreshToken) throws IOException {
        Optional<User> refreshTokenUser = userRepository.findByRefreshToken(refreshToken);
        if(refreshTokenUser.isPresent()){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); //403 -> 토큰 재발급 요청
            this.createJsonForErrorResponse(response, ErrorCode.TOKEN_EXPIRED);
        }
        else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            this.createJsonForErrorResponse(response, ErrorCode.UNAUTHORIZED_USER);
        }
    }

    // token 재발행 요청 -> 검증 후 재발행
    private void tokenREISSUED(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String refreshToken = jwtService.extractRefreshToken(request).get();
        Optional<User> refreshTokenUser = userRepository.findByRefreshToken(refreshToken);
        if(refreshTokenUser.isPresent()){
            String newAccessToken = jwtService.createAccessToken(refreshTokenUser.get().getUserId());
            String newRefreshToken = jwtService.createRefreshToken();
            this.saveAuthentication(refreshTokenUser.get());
            jwtService.sendAccessAndRefreshToken(response, newAccessToken, newRefreshToken);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("timestamp", LocalDateTime.now());
            responseData.put("message", "ReIssued AccessToken , RefreshToken");
            responseData.put("result", true);

            Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new GsonConfig()).setPrettyPrinting().create();
            String json = gson.toJson(responseData);
            response.getWriter().write(json);
        }
        else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            this.createJsonForErrorResponse(response, ErrorCode.UNAUTHORIZED_USER);
        }
    }

    private void createJsonForErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getHttpStatus().name())
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .build();

        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new GsonConfig()).setPrettyPrinting().create();
        String json = gson.toJson(errorResponse);
        response.getWriter().write(json);
    }
}

package com.peoples.api.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.peoples.api.config.GsonConfig;
import com.peoples.api.exception.ErrorCode;
import com.peoples.api.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(ErrorCode.UNAUTHORIZED_USER.getHttpStatus().value())
                .error(ErrorCode.UNAUTHORIZED_USER.getHttpStatus().name())
                .code(ErrorCode.UNAUTHORIZED_USER.name())
                .message(ErrorCode.UNAUTHORIZED_USER.getMessage())
                .build();

        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new GsonConfig()).setPrettyPrinting().create();
        String json = gson.toJson(errorResponse);
        response.getWriter().write(json);
    }
}

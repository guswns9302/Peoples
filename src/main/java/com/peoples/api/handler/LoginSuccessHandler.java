package com.peoples.api.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.peoples.api.config.GsonConfig;
import com.peoples.api.domain.User;
import com.peoples.api.dto.response.UserResponse;
import com.peoples.api.repository.UserRepository;
import com.peoples.api.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        String userId = this.extractUserId(authentication);
        String accessToken = jwtService.createAccessToken(userId);
        String refreshToken = jwtService.createRefreshToken();
        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);

        Optional<User> user = userRepository.findByUserId(userId);
        user.get().updateLastLogin(LocalDateTime.now());

        UserResponse.from(user.get());

        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new GsonConfig()).disableHtmlEscaping().setPrettyPrinting().create();
        response.getWriter().write(gson.toJson(UserResponse.from(user.get())));
    }


    private String extractUserId(Authentication authentication){
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}

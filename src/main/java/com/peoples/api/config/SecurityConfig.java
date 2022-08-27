package com.peoples.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoples.api.filter.JsonLoginAuthenticationFilter;
import com.peoples.api.filter.JwtAuthenticationProcessingFilter;
import com.peoples.api.handler.LoginSuccessHandler;
import com.peoples.api.handler.LoginFailureHandler;
import com.peoples.api.repository.UserRepository;
import com.peoples.api.service.JwtService;
import com.peoples.api.service.SecurityUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
@ConditionalOnDefaultWebSecurity
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfig {
    private final UserRepository userRepository;
    private final SecurityUserService securityUserService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    private static final String[] SWAGGER_WHITE_LIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .httpBasic().disable()
            .formLogin().disable()
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)// jwt 인증으로 세션 생성 안함.
            .and()
            .authorizeRequests()
                .antMatchers(SWAGGER_WHITE_LIST).permitAll()
                .antMatchers("/api/v1/signin", "/api/v1/signup", "/api/v1/signup/verification", "/api/v1/user/password", "/api/v1/downloadIMG", "/api/v1/email/auth", "/api/v1/login/oauth/**").permitAll()
                .antMatchers("/api/v1/**").hasRole("USER")
                .anyRequest().authenticated();

        http.addFilterAfter(jsonLoginAuthenticationFilter(), LogoutFilter.class);
        http.addFilterBefore(jwtAuthenticationProcessingFilter(), JsonLoginAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(securityUserService);
        return new ProviderManager(provider);
    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler(){
        return new LoginSuccessHandler(jwtService, userRepository);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler(){
        return new LoginFailureHandler();
    }

    @Bean
    public JsonLoginAuthenticationFilter jsonLoginAuthenticationFilter(){
        JsonLoginAuthenticationFilter jsonLoginAuthenticationFilter = new JsonLoginAuthenticationFilter(objectMapper);
        jsonLoginAuthenticationFilter.setAuthenticationManager(authenticationManager());
        jsonLoginAuthenticationFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
        jsonLoginAuthenticationFilter.setAuthenticationFailureHandler(loginFailureHandler());
        return jsonLoginAuthenticationFilter;
    }

    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter(){
        JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter = new JwtAuthenticationProcessingFilter(jwtService, userRepository);
        return jwtAuthenticationProcessingFilter;
    }
}

package com.peoples.api.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.peoples.api.domain.User;
import com.peoples.api.domain.enumeration.Role;
import com.peoples.api.dto.response.SocialLoginResponse;
import com.peoples.api.dto.response.UserResponse;
import com.peoples.api.exception.CustomException;
import com.peoples.api.exception.ErrorCode;
import com.peoples.api.repository.UserRepository;
import com.peoples.api.service.responseMap.ResponseMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocialLoginService extends ResponseMap {

    private final InMemoryClientRegistrationRepository inMemoryClientRegistrationRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    private Map<String, Object> getToken(String code, ClientRegistration provider) {
        return WebClient.create()
                        .post()
                        .uri(provider.getProviderDetails().getTokenUri())
                        .headers(header -> {
                            header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                            header.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
                        })
                        .bodyValue(tokenRequest(code, provider))
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String,Object>>() {})
                        .block();
    }

    private MultiValueMap<String, String> tokenRequest(String code, ClientRegistration provider){
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code",code);
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", provider.getRedirectUri());
        formData.add("client_secret", provider.getClientSecret());
        formData.add("client_id", provider.getClientId());
        return formData;
    }

    private Map<String, String> getUserInfo(String provider_name, Map<String,Object> tokenResponse, ClientRegistration provider){
        String socialUser = getUserforToken(tokenResponse, provider);
        Map<String, String> oauth2User = new HashMap<>();
        String email = "";
        String nickname = "";

        JsonObject jsonObject = (JsonObject) JsonParser.parseString(socialUser);
        if(provider_name.equals("kakao")){
            JsonObject user_properties = (JsonObject) jsonObject.get("properties");
            JsonObject user_account = (JsonObject) jsonObject.get("kakao_account");
            email = user_account.get("email").toString();
            nickname = user_properties.get("nickname").toString();
        }
        else if(provider_name.equals("naver")){
            JsonObject user_response = (JsonObject) jsonObject.get("response");
            email = user_response.get("email").toString();
            nickname = user_response.get("nickname").toString();
        }
        toSubstringDoubleQuotation(oauth2User,email,nickname);
        return oauth2User;
    }

    @Transactional
    public SocialLoginResponse login(String provider_name, String code) {
        ClientRegistration provider = inMemoryClientRegistrationRepository.findByRegistrationId(provider_name);
        Map<String,Object> tokenResponse = getToken(code, provider);
        log.debug("oauth2 token : {}", tokenResponse);

        Map<String, String> oauth2user = getUserInfo(provider_name, tokenResponse, provider);
        log.debug("oauth2 user : {} / nickname : {}", oauth2user.get("email"), oauth2user.get("nickname"));
        Optional<User> existUser = userRepository.findByUserId(oauth2user.get("email"));
        if(existUser.isPresent()){
            // 기존 사용자 존재 ( 동일한 이메일 )
            existUser.get().snsCheck(provider_name);
            return this.userProvideJWT(existUser.get());
        }
        else{
            // 기존 사용자가 없음 ( 신규 가입 )
            User newUser = User.builder()
                        .userId(oauth2user.get("email"))
                        .nickname(oauth2user.get("nickname"))
                        .role(Role.ROLE_USER)
                        .snsKakao(false)
                        .snsNaver(false)
                        .emailAuthentication(true)
                        .userBlock(false)
                        .userState(false)
                        .userPause(false)
                        .kickoutCnt(0)
                        .build();
            User save = userRepository.save(newUser);
            save.snsCheck(provider_name);
            return this.userProvideJWT(save);
        }
    }
// ------------------------------------------------------------------------------------------------------------------
    @Transactional
    public SocialLoginResponse oauth2Login(String provider, String token) {
        Map<String, String> oauth2user = this.getUserInfoFromAccessToken(provider, token);
        log.debug("oauth2 user : {} / nickname : {}", oauth2user.get("email"), oauth2user.get("nickname"));
        Optional<User> existUser = userRepository.findByUserId(oauth2user.get("email"));
        if(existUser.isPresent()){
            // 기존 사용자 존재 ( 동일한 이메일 )
            existUser.get().snsCheck(provider);
            return this.userProvideJWT(existUser.get());
        }
        else{
            // 기존 사용자가 없음 ( 신규 가입 )
            User newUser = User.builder()
                    .userId(oauth2user.get("email"))
                    .nickname(oauth2user.get("nickname"))
                    .role(Role.ROLE_USER)
                    .snsKakao(false)
                    .snsNaver(false)
                    .emailAuthentication(true)
                    .userBlock(false)
                    .userState(false)
                    .userPause(false)
                    .kickoutCnt(0)
                    .build();
            User save = userRepository.save(newUser);
            save.snsCheck(provider);
            return this.userProvideJWT(save);
        }

    }

    private Map<String, String> getUserInfoFromAccessToken(String provider, String token) {
        String socialUser = this.getUser(token, provider);
        Map<String, String> oauth2User = new HashMap<>();
        String email = "";
        String nickname = "";

        JsonObject jsonObject = (JsonObject) JsonParser.parseString(socialUser);
        if(provider.equals("kakao")){
            JsonObject user_properties = (JsonObject) jsonObject.get("properties");
            JsonObject user_account = (JsonObject) jsonObject.get("kakao_account");
            email = user_account.get("email").toString();
            nickname = user_properties.get("nickname").toString();
        }
        else if(provider.equals("naver")){
            JsonObject user_response = (JsonObject) jsonObject.get("response");
            email = user_response.get("email").toString();
            nickname = user_response.get("nickname").toString();
        }
        toSubstringDoubleQuotation(oauth2User,email,nickname);
        return oauth2User;
    }

    private String getUser(String token, String provider){
        if(provider.equals("kakao")){
            return WebClient.create()
                    .get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .headers(header -> header.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        }
        else if(provider.equals("naver")){
            return WebClient.create()
                    .get()
                    .uri("https://openapi.naver.com/v1/nid/me")
                    .headers(header -> header.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        }
        else{
            throw new CustomException(ErrorCode.ILLEGAL_LOGIN);
        }
    }

    // 토큰으로 받아온 정보 parsing
    private void toSubstringDoubleQuotation(Map<String, String> oauth2User, String email, String nickname){
        oauth2User.put("email", email.substring(1, email.length()-1));
        oauth2User.put("nickname", nickname.substring(1, nickname.length()-1));
    }

    // access token 으로 oauth sever에 회원 정보 요청
    private String getUserforToken(Map<String,Object> tokenResponse, ClientRegistration provider){
        return WebClient.create()
                .get()
                .uri(provider.getProviderDetails().getUserInfoEndpoint().getUri())
                .headers(header -> header.setBearerAuth(tokenResponse.get("access_token").toString()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // 회원 정보 확인 후 jwt 토큰을 발급
    private SocialLoginResponse userProvideJWT(User user){
        String accessToken = jwtService.createAccessToken(user.getUserId());
        String refreshToken = jwtService.createRefreshToken();
        log.debug("accessToken : {}", accessToken);
        log.debug("refreshToken : {}", refreshToken);
        user.updateRefreshToken(refreshToken);
        return SocialLoginResponse.from(user, accessToken, refreshToken);
    }
}

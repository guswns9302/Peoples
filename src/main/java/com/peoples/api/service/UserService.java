package com.peoples.api.service;

import com.peoples.api.domain.EmailAuthToken;
import com.peoples.api.domain.StudyMember;
import com.peoples.api.domain.User;
import com.peoples.api.domain.enumeration.Role;
import com.peoples.api.dto.response.DeleteUserResponse;
import com.peoples.api.dto.response.StudyResponse;
import com.peoples.api.dto.response.UserResponse;
import com.peoples.api.dto.response.UserStudyHistoryResponse;
import com.peoples.api.exception.CustomException;
import com.peoples.api.exception.ErrorCode;
import com.peoples.api.handler.LoginSuccessHandler;
import com.peoples.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtService jwtService;

    private static final String SAVE_IMG_DIRECTORY = System.getProperty("user.home");

    @Transactional
    public UserResponse createUser(Map<String, Object> param, MultipartFile file, HttpServletResponse response) throws IOException {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        // 이메일 중복 검증
        if(this.verificationEmail(param)){
            // 비밀번호 - 비밀번호 확인 체크
            if(param.get("password").toString().equals(param.get("password_check").toString())){
                String imgName = "peoples_logo.png";

                User newUser = User.builder()
                        .userId(param.get("userId").toString())
                        .password(passwordEncoder.encode(param.get("password").toString()))
                        .nickname(param.get("nickname").toString())
                        .img(imgName)
                        .role(Role.ROLE_USER)
                        .snsKakao(false)
                        .snsNaver(false)
                        .emailAuthentication(false)
                        .userBlock(false)
                        .userState(false)
                        .userPause(false)
                        .pushStart(true)
                        .pushImminent(true)
                        .pushDayAgo(true)
                        .kickoutCnt(0)
                        .build();
                User save = userRepository.save(newUser);
                if(save != null){
                    // 이메일 인증 메일 발송
                    emailService.createEmailAuthToken(save.getUserId(), save.getNickname());

                    if(!file.isEmpty()){
                        UUID uuid = UUID.randomUUID();
                        imgName = uuid + "_" + file.getOriginalFilename();
                        File saveFile = new File(SAVE_IMG_DIRECTORY + "/profile/" + imgName);
                        try {
                            file.transferTo(saveFile);
                            save.updateProfileImg(imgName);
                        }
                        catch (IOException e) {
                            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                        }
                    }

                    // 회원정보가 저장 ok
                    //return true;
                    String accessToken = jwtService.createAccessToken(save.getUserId());
                    String refreshToken = jwtService.createRefreshToken();
                    jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);

                    save.updateLastLogin(LocalDateTime.now());
                    return UserResponse.from(save);
                }
                else{
                    // 회원정보가 저장 실패
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                }
            }
            else{
                // 비밀번호 불일치
                throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
            }
        }
        else{
            // 이메일 검증 실패
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    public Boolean verificationEmail(Map<String, Object> param) {
        Optional<User> existUser = userRepository.findByUserId(param.get("userId").toString());
        if(existUser.isPresent()){
            // 중복된 이메일이 있음 false
            return false;
        }
        else{
            // 중복된 이메일이 없음 true
            return true;
        }
    }

    @Transactional
    public DeleteUserResponse deleteUser(String userId) {
        Optional<User> user = userRepository.findByUserId(userId);

        if(user.isPresent()){
            List<StudyMember> studyMemberList = user.get().getStudyMemberList();
            if(!studyMemberList.isEmpty()){
                List<StudyResponse> studyResponseList = new ArrayList<>();
                studyMemberList.forEach(data->{
                    if(data.getUserRole().equals("스터디장")){
                        studyResponseList.add(StudyResponse.from(data.getStudy()));
                    }
                });
                if(!studyResponseList.isEmpty()){
                    return DeleteUserResponse.builder().result(false).studyMemberList(studyResponseList).build();
                }
            }
            userRepository.delete(user.get());
            return DeleteUserResponse.builder().result(true).build();
        }
        else{
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public List<UserStudyHistoryResponse> history(String userId) {
        Optional<User> user = userRepository.findByUserId(userId);
        if(user.isPresent()){
            List<UserStudyHistoryResponse> userStudyHistoryList = user.get().getUserStudyHistoryList().stream().map(UserStudyHistoryResponse::from).collect(Collectors.toList());
            return userStudyHistoryList;
        }
        else{
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Transactional
    public boolean emailAuth(String token){
        Optional<EmailAuthToken> findToken = emailService.getToken(token);
        if(findToken.isEmpty()){
            return false;
        }
        else{
            Optional<User> emailAuthUser = userRepository.findByUserId(findToken.get().getUserId());
            emailAuthUser.get().successEmailAuth();
            findToken.get().useToken();
            return true;
        }
    }

    @Transactional
    public Boolean tempPassword(String userId){
        Optional<User> user = userRepository.findByUserId(userId);
        if(user.isPresent()){
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String temp_pw = this.getRandomPw();
            user.get().updatePassword(passwordEncoder.encode(temp_pw));
            emailService.sendTempPw(userId,temp_pw);
            return true;
        }
        else{
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    public String getRandomPw(){
        StringBuffer key = new StringBuffer();
        Random rnd = new Random();

        for (int i = 0; i < 6; i++) {
            int index = rnd.nextInt(3);
            switch (index) {
                case 0:
                    key.append(((int) (rnd.nextInt(26)) + 97));
                    break;
                case 1:
                    key.append(((int) (rnd.nextInt(26)) + 65));
                    break;
                case 2:
                    key.append((rnd.nextInt(10)));
                    break;
            }
        }
        return key.toString();
    }

    public Boolean reSendAuthMail(User user) {
        return emailService.createEmailAuthToken(user.getUserId(), user.getNickname());
    }

    @Transactional
    public Map<String,Object> updateUser(String userId, Map<String, Object> param, MultipartFile file) {
        Optional<User> user = userRepository.findByUserId(userId);
        if(user.isPresent()){
            if(!param.get("password").toString().equals("") && !param.get("password_check").toString().equals("") && !param.get("old_password").toString().equals("")){
                log.debug("old_password : {}", param.get("old_password").toString());
                log.debug("password : {}", param.get("password").toString() );
                log.debug("password_check : {}", param.get("password_check").toString());
                if(param.get("password").toString().equals(param.get("password_check").toString())){
                    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                    if(passwordEncoder.matches(param.get("old_password").toString(), user.get().getPassword())){
                        user.get().updatePassword(passwordEncoder.encode(param.get("password").toString()));
                    }
                    else {
                        throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
                    }
                }
                else{
                    throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
                }
            }
            if(!param.get("nickname").toString().equals("")){
                log.debug("nickname : {}", param.get("nickname").toString());
                user.get().updateNickname(param.get("nickname").toString());
            }
            if(!file.isEmpty()){
                log.debug("file : {}", file.getOriginalFilename());
                String oldImg = user.get().getImg();
                File deleteFile = new File(oldImg);
                deleteFile.delete();

                UUID uuid = UUID.randomUUID();
                String newImg = uuid + "_" + file.getOriginalFilename();
                File saveFile = new File(SAVE_IMG_DIRECTORY + "/profile/" + newImg);
                try {
                    file.transferTo(saveFile);
                    user.get().updateProfileImg(newImg);
                } catch (Exception e) {
                    throw new CustomException(ErrorCode.IMG_NOT_FOUND);
                }
            }
            String fileName = "fileName";
            return Map.of("nickname", user.get().getNickname(), "img", ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/downloadIMG").queryParam(fileName, user.get().getImg()).toUriString());
        }
        else{
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public Map<String,Object> findUser(String userId) {
        Optional<User> user = userRepository.findByUserId(userId);
        if(user.isPresent()){
            String fileName = "fileName";
            return Map.of("userId", user.get().getUserId(),
                    "nickname", user.get().getNickname(),
                    "img", ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/downloadIMG").queryParam(fileName, user.get().getImg()).toUriString(),
                    "sns_kakao", user.get().isSnsKakao(),
                    "sns_naver", user.get().isSnsNaver());
        }
        else{
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public Boolean checkPassword(Map<String, String> param) {
        User user = userRepository.findByUserId(param.get("userId")).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if(passwordEncoder.matches(param.get("password"), user.getPassword())){
            return true;
        }
        else{
            return false;
        }
    }

    @Transactional
    public void pushStart(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.changePushStart(user.isPushStart());
    }

    @Transactional
    public void pushImminent(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.changePushImminent(user.isPushImminent());
    }

    @Transactional
    public void pushDay(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.changePushDay(user.isPushDayAgo());
    }

    @Transactional(readOnly = true)
    public Boolean checkEmail(String userId) {
        Optional<User> user = userRepository.findByUserId(userId);
        if (user.isPresent()) {
            return user.get().isEmailAuthentication();
        } else {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }
}

package com.peoples.api.service;

import com.peoples.api.domain.EmailAuthToken;
import com.peoples.api.domain.StudyMember;
import com.peoples.api.domain.User;
import com.peoples.api.domain.enumeration.Role;
import com.peoples.api.dto.response.StudyResponse;
import com.peoples.api.dto.response.UserResponse;
import com.peoples.api.dto.response.UserStudyHistoryResponse;
import com.peoples.api.exception.CustomException;
import com.peoples.api.exception.ErrorCode;
import com.peoples.api.repository.UserRepository;
import com.peoples.api.service.responseMap.ResponseMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService extends ResponseMap {

    private final UserRepository userRepository;
    private final EmailService emailService;

    private static final String SAVE_IMG_DIRECTORY = System.getProperty("user.home");

    @Transactional
    public Map<String,Object> createUser(Map<String, Object> param, MultipartFile file){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        // 이메일 중복 검증
        if(Boolean.parseBoolean(this.verificationEmail(param).get("result").toString())){
            // 비밀번호 - 비밀번호 확인 체크
            if(param.get("password").toString().equals(param.get("password_check").toString())){
                String imgName = "5bda7b70-4557-4fea-8f7e-5797f0042a23_peoples_logo.png";
                // img 등록
                if(!file.getOriginalFilename().equals("")){
                    UUID uuid = UUID.randomUUID();
                    imgName = uuid + "_" + file.getOriginalFilename();
                    File saveFile = new File(SAVE_IMG_DIRECTORY + "/" + imgName);
                    try {
                        file.transferTo(saveFile);
                    }
                    catch (IOException e) {
                        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                    }
                }
                log.debug("profile img : {}", imgName);
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
                        .kickoutCnt(0)
                        .build();
                User save = userRepository.save(newUser);

                if(save != null){
                    // 이메일 인증 메일 발송
                    emailService.createEmailAuthToken(save.getUserId(), save.getNickname());
                    // 회원정보가 저장 ok
                    return this.responseMap("회원가입 성공", true);
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

    public Map<String,Object> verificationEmail(Map<String, Object> param) {
        Optional<User> existUser = userRepository.findByUserId(param.get("userId").toString());
        if(existUser.isPresent()){
            return this.responseMap("중복된 Email 이 있습니다.",false);
        }
        else{
            return this.responseMap("중복된 Email 이 없습니다.", true);
        }
    }

    @Transactional
    public Map<String,Object> profileChange(String userId, MultipartFile file) {
        Optional<User> user = userRepository.findByUserId(userId);
        if(user.isPresent()){
            String oldImg = user.get().getImg();
            File deleteFile = new File(oldImg);
            deleteFile.delete();

            UUID uuid = UUID.randomUUID();
            String newImg = uuid + "_" + file.getOriginalFilename();
            File saveFile = new File(SAVE_IMG_DIRECTORY + "/" + newImg);
            try {
                file.transferTo(saveFile);
                user.get().updateProfileImg(newImg);
                String fileName = "fileName";

                return this.responseMap("프로필이미지 변경 성공", ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/downloadIMG").queryParam(fileName, user.get().getImg()).toUriString());
            } catch (Exception e) {
                throw new CustomException(ErrorCode.IMG_NOT_FOUND);
            }
        }
        else{
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Transactional
    public Map<String,Object> deleteUser(String userId) {
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
                    return this.responseMap("스터디장을 위임한 뒤 탈퇴하십시오.", studyResponseList);
                }
            }
            userRepository.delete(user.get());
            return this.responseMap("회원 탈퇴 성공", true);
        }
        else{
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Transactional
    public Map<String,Object> changeNickname(String userId, Map<String, Object> param) {
        Optional<User> user = userRepository.findByUserId(userId);
        if(user.isPresent()){
            user.get().updateNickname(param.get("nickname").toString());
            return this.responseMap("닉네임이 변경되었습니다.", UserResponse.from(user.get()));
        }
        else{
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public Map<String,Object> history(String userId) {
        Optional<User> user = userRepository.findByUserId(userId);
        if(user.isPresent()){
            List<UserStudyHistoryResponse> userStudyHistoryList = user.get().getUserStudyHistoryList().stream().map(UserStudyHistoryResponse::from).collect(Collectors.toList());
            return this.responseMap("참여한 스터디 기록", userStudyHistoryList);
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
    public Map<String,Object> tempPassword(String userId){
        Optional<User> user = userRepository.findByUserId(userId);
        if(user.isPresent()){
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String temp_pw = this.getRandomPw();
            user.get().updatePassword(passwordEncoder.encode(temp_pw));
            emailService.sendTempPw(userId,temp_pw);
            return this.responseMap("임시 비밀번호 전송 성공", true);
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

    public Map<String,Object> reSendAuthMail(User user) {
        boolean result = emailService.createEmailAuthToken(user.getUserId(), user.getNickname());
        return this.responseMap("인증 메일 재발송!", result);
    }
}

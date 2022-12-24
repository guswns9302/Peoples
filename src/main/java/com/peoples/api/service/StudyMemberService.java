package com.peoples.api.service;

import com.peoples.api.domain.EmailAuthToken;
import com.peoples.api.domain.StudyMember;
import com.peoples.api.domain.User;
import com.peoples.api.domain.enumeration.Role;
import com.peoples.api.domain.security.SecurityUser;
import com.peoples.api.dto.response.DeleteUserResponse;
import com.peoples.api.dto.response.StudyMemberResponse;
import com.peoples.api.dto.response.StudyResponse;
import com.peoples.api.dto.response.UserStudyHistoryResponse;
import com.peoples.api.exception.CustomException;
import com.peoples.api.exception.ErrorCode;
import com.peoples.api.repository.StudyMemberRepository;
import com.peoples.api.repository.StudyRepository;
import com.peoples.api.repository.UserRepository;
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
public class StudyMemberService {

    private final StudyMemberRepository studyMemberRepository;
    private final AlarmService alarmService;

    @Transactional
    public List<StudyMemberResponse> getList(long studyId) {
        return studyMemberRepository.findByStudy_StudyId(studyId).stream().map(StudyMemberResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public boolean updateRole(Map<String,Object> param) {
        Optional<StudyMember> studyMember = studyMemberRepository.findById(Long.parseLong(param.get("studyMemberId").toString()));
        if(studyMember.isPresent()){
            if(studyMember.get().getUserRole().equals("스터디장")){
                throw new CustomException(ErrorCode.MASTER_DO_NOT_CHANGE);
            }
            else{
                studyMember.get().updateRole(param.get("userRole").toString());
                return true;
            }
        }
        else{
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }

    @Transactional
    public boolean updateManager(Map<String, Object> param) {
        Optional<StudyMember> studyMember = studyMemberRepository.findById(Long.parseLong(param.get("studyMemberId").toString()));
        if(studyMember.isPresent()){
            studyMember.get().updateManager(studyMember.get().isUserManager());
            alarmService.changeManager(studyMember.get());
            return true;
        }
        else{
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }

    @Transactional
    public boolean expire(long studyMemberId, User user) {
        Optional<StudyMember> studyMember = studyMemberRepository.findById(studyMemberId);
        Optional<StudyMember> my = studyMemberRepository.findByUser_UserIdAndStudy_StudyId(user.getUserId(), studyMember.get().getStudy().getStudyId());
        if(studyMember.isPresent() && my.isPresent()){
            if(studyMember.get().getUser().getUserId().equals(my.get().getUser().getUserId())){
                throw new CustomException(ErrorCode.DO_NOT_SELF_EXPIRE);
            }
            if(my.get().isUserManager()){
                if(studyMember.get().getUserRole().equals("스터디장")){
                    throw new CustomException(ErrorCode.MASTER_DO_NOT_EXPIRE);
                }
                else{
                    alarmService.expire(studyMember.get());
                    studyMemberRepository.delete(studyMember.get());
                    return true;
                }
            }
            else{
                throw new CustomException(ErrorCode.NOT_MANAGER);
            }
        }
        else {
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }

    @Transactional
    public boolean changeMaster(User user, Map<String, Long> param) {
        Optional<StudyMember> studyMember = studyMemberRepository.findById(param.get("studyMemberId"));
        Optional<StudyMember> my = studyMemberRepository.findByUser_UserIdAndStudy_StudyId(user.getUserId(), studyMember.get().getStudy().getStudyId());

        if(studyMember.isPresent() && my.isPresent()){
            my.get().updateRole("");
            studyMember.get().updateRole("스터디장");
            studyMember.get().updateManager(false);
            return true;
        }
        else{
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }
}

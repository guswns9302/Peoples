package com.peoples.api.service;

import com.peoples.api.domain.User;
import com.peoples.api.domain.UserSchedule;
import com.peoples.api.domain.enumeration.Status;
import com.peoples.api.domain.security.SecurityUser;
import com.peoples.api.dto.request.UserScheduleRequest;
import com.peoples.api.dto.response.UserScheduleResponse;
import com.peoples.api.exception.CustomException;
import com.peoples.api.exception.ErrorCode;
import com.peoples.api.repository.UserRepository;
import com.peoples.api.repository.UserScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserScheduleService {

    private final UserRepository userRepository;
    private final UserScheduleRepository userScheduleRepository;

    @Transactional(readOnly = true)
    public List<UserScheduleResponse> findUserSchedule(String userId) {
        Optional<User> byUserId = userRepository.findByUserId(userId);

        List<UserScheduleResponse> userScheduleList = new ArrayList<>();
        if(!byUserId.get().getUserScheduleList().isEmpty()){
            userScheduleList = byUserId.get().getUserScheduleList().stream().map(UserScheduleResponse::from).collect(Collectors.toList());
        }
        return userScheduleList;
    }

    @Transactional
    public List<UserScheduleResponse> createUserSchedule(UserScheduleRequest userScheduleRequest, SecurityUser user) {
        log.debug("request : {}, userId : {}", userScheduleRequest, user.getUsername());

        UserSchedule newUserSchedule = UserSchedule.builder()
                                                    .scheduleName(userScheduleRequest.getScheduleName())
                                                    .scheduleDate(userScheduleRequest.getScheduleDate())
                                                    .user(user.getUser())
                                                    .status(Status.RUNNING)
                                                    .build();

        UserSchedule saveSchedule = userScheduleRepository.save(newUserSchedule);
        if(saveSchedule != null){
            return this.findUserSchedule(user.getUsername());
        }
        else{
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public List<UserScheduleResponse> updateStatus(long scheduleId) {
        log.debug("scheduleId : {}", scheduleId);
        Optional<UserSchedule> schedule = userScheduleRepository.findById(scheduleId);
        if(schedule.isPresent()){
            if(schedule.get().getStatus() == Status.RUNNING){
                schedule.get().updateStatus(Status.STOP);
            }
            else if(schedule.get().getStatus() == Status.STOP){
                schedule.get().updateStatus(Status.RUNNING);
            }
            return this.findUserSchedule(schedule.get().getUser().getUserId());
        }
        else{
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }

    @Transactional
    public boolean updateSchedule(Map<String, Object> param) {
        Optional<UserSchedule> schedule = userScheduleRepository.findById(Long.parseLong(param.get("scheduleId").toString()));
        if(schedule.isPresent()){
            System.out.println(param.get("scheduleName"));
            if(!param.get("scheduleName").equals("")){
                schedule.get().updateName(param.get("scheduleName").toString());
            }
            else{
                userScheduleRepository.delete(schedule.get());
            }
            return true;
        }
        else{
            throw new CustomException(ErrorCode.RESULT_NOT_FOUND);
        }
    }
}

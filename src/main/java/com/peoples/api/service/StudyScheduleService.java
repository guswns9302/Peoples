package com.peoples.api.service;

import com.peoples.api.domain.StudyMember;
import com.peoples.api.domain.StudySchedule;
import com.peoples.api.repository.UserRepository;
import com.peoples.api.service.responseMap.ResponseMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudyScheduleService extends ResponseMap {

    private final UserRepository userRepository;

    public Map<String,Object> findSchedule(String userId) {
        List<StudyMember> studyMemberList = userRepository.findByUserId(userId).get().getStudyMemberList();
        if(!studyMemberList.isEmpty()){
            Map<Long, List<StudySchedule>> joinStudyScheduleList = new HashMap<>();
            studyMemberList.forEach(data->{
                List<StudySchedule> studyScheduleList = data.getStudy().getStudyScheduleList();
                if(!studyScheduleList.isEmpty()){
                    joinStudyScheduleList.put(data.getStudy().getStudyId(), studyScheduleList);
                }
            });
            return this.responseMap("참여 중인 스터디의 스케쥴 목록 조회", joinStudyScheduleList);
        }
        else{
            return this.responseMap("스케쥴이 없습니다.", null);
        }
    }
}

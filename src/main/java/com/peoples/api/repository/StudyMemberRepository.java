package com.peoples.api.repository;

import com.peoples.api.domain.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {
    List<StudyMember> findByStudy_StudyId(long studyId);

    Optional<StudyMember> findByUser_UserIdAndStudy_StudyId(String userId, long studyId);

    Optional<StudyMember> findByUser_UserId(String userId);
}

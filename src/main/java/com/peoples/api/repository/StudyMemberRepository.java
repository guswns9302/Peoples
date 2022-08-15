package com.peoples.api.repository;

import com.peoples.api.domain.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {
    List<StudyMember> findByUser_UserId(String userId);
}

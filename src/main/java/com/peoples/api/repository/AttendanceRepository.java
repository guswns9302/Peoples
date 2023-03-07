package com.peoples.api.repository;

import com.peoples.api.domain.Attendance;
import com.peoples.api.domain.Study;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserIdAndStudySchedule_StudyScheduleId(String userId, long studyScheduleId);
}

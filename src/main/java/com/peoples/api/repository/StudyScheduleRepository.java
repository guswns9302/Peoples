package com.peoples.api.repository;

import com.peoples.api.domain.StudySchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudyScheduleRepository extends JpaRepository<StudySchedule, Long> {
    List<StudySchedule> findByStudyScheduleDate(LocalDate now);
    List<StudySchedule> findByRepeatNumber(Long number);
}

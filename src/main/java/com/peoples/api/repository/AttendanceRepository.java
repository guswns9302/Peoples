package com.peoples.api.repository;

import com.peoples.api.domain.Attendance;
import com.peoples.api.domain.Study;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
}

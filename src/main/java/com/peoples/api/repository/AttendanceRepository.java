package com.peoples.api.repository;

import com.peoples.api.domain.Attendance;
import com.peoples.api.domain.Study;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
}

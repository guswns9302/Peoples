package com.peoples.api.repository;

import com.peoples.api.domain.UserSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserScheduleRepository extends JpaRepository<UserSchedule, Long> {

    List<UserSchedule> findAllByUser_UserId(String userId);
}

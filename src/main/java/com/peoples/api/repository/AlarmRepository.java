package com.peoples.api.repository;

import com.peoples.api.domain.Alarm;
import com.peoples.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    List<Alarm> findByUser_UserId(String userId);
}

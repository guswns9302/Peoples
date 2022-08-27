package com.peoples.api.repository;

import com.peoples.api.domain.Study;
import com.peoples.api.domain.StudyNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyNotificationRepository extends JpaRepository<StudyNotification, Long> {

    List<StudyNotification> findByPin(boolean pin);
}

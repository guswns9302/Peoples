package com.peoples.api.repository;

import com.peoples.api.domain.Study;
import com.peoples.api.domain.StudyNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyNotificationRepository extends JpaRepository<StudyNotification, Long> {

    Optional<StudyNotification> findByPin(boolean pin);

    Optional<StudyNotification> findByPinAndStudy_StudyId(boolean b, Long studyId);
}

package com.peoples.api.repository;

import com.peoples.api.domain.UserStudyHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStudyHistoryRepository extends JpaRepository<UserStudyHistory, Long> {
}

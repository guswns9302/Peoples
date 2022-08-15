package com.peoples.api.repository;

import com.peoples.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserId(String userId);

    Optional<User> findByRefreshToken(String refreshToken);
}

package com.peoples.api.service.admin;

import com.peoples.api.domain.User;
import com.peoples.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    public Map<String, Object> getUsers() {
        List<User> users = userRepository.findAll();
        return null;
    }
}

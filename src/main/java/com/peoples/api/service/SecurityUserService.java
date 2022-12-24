package com.peoples.api.service;

import com.peoples.api.domain.User;
import com.peoples.api.domain.security.SecurityUser;
import com.peoples.api.exception.CustomException;
import com.peoples.api.exception.ErrorCode;
import com.peoples.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityUserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Optional<User> byUserId = userRepository.findByUserId(userId);
        if(byUserId.isPresent()){
            return SecurityUser.of(byUserId.get());
        }
        else{
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }
}

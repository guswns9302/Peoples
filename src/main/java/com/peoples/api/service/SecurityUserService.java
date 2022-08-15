package com.peoples.api.service;

import com.peoples.api.domain.security.SecurityUser;
import com.peoples.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityUserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return SecurityUser.of(userRepository.findByUserId(userId).orElseThrow(() -> new UsernameNotFoundException(userId)));
    }
}

package com.peoples.api.domain.security;

import com.peoples.api.domain.User;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@ToString
@Builder
public class SecurityUser implements UserDetails {

    @EqualsAndHashCode.Exclude
    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authority= new HashSet<>();
        authority.add(new SimpleGrantedAuthority(this.user.getRole().toString()));
        return authority;
    }

    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    @EqualsAndHashCode.Include
    public String getUsername() {
        return this.user.getUserId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public SecurityUser(User user) {
        this.user = user;
    }

    public static SecurityUser of(User user) {
        return SecurityUser.builder().user(user).build();
    }
}

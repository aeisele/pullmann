package com.andreaseisele.pullmann.security;

import com.andreaseisele.pullmann.github.dto.User;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class GitHubUserDetails implements UserDetails {

    private final User user;
    private final String accessToken;
    private final Set<? extends GrantedAuthority> authorities;
    private final LocalDateTime expiry;

    public GitHubUserDetails(User user,
                             String accessToken,
                             Set<? extends GrantedAuthority> authorities,
                             LocalDateTime expiry) {
        this.user = user;
        this.accessToken = accessToken;
        this.authorities = authorities;
        this.expiry = expiry;
    }

    @Override
    public String getUsername() {
        return user.name();
    }

    @Override
    public String getPassword() {
        return accessToken;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.copyOf(authorities);
    }

    @Override
    public boolean isAccountNonExpired() {
        return expiry.isAfter(LocalDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return expiry.isAfter(LocalDateTime.now());
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public User getUser() {
        return user;
    }

    public String getAccessToken() {
        return accessToken;
    }

}

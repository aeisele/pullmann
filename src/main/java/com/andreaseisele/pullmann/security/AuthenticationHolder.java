package com.andreaseisele.pullmann.security;

import com.andreaseisele.pullmann.github.error.GitHubAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationHolder {

    /**
     * Retrieves the current authentication from security context.
     * @return the current authentication.
     * @throws GitHubAuthenticationException if no authentication or an invalid one exists
     */
    public static UsernamePasswordAuthenticationToken currentAuthentication() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken token) {
            return token;
        }
        throw new GitHubAuthenticationException("no or invalid authentication in security context");
    }

}

package com.andreaseisele.pullmann.github;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class GitHubAuthenticator extends Authenticator {

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            // TODO
        }
        return null;
    }
}

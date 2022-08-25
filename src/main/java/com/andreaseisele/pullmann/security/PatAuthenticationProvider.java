package com.andreaseisele.pullmann.security;

import static java.util.Objects.requireNonNull;


import com.andreaseisele.pullmann.github.GitHubClient;
import com.andreaseisele.pullmann.github.dto.User;
import com.andreaseisele.pullmann.github.error.GitHubHttpStatusException;
import com.andreaseisele.pullmann.github.result.UserResult;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * This provider uses the local login information to authenticate against GitHub.
 * The 'credentials' field of the local authentication must carry the personal access token.
 */
@Component
public class PatAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(PatAuthenticationProvider.class);

    private final GitHubClient gitHubClient;

    public PatAuthenticationProvider(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        requireNonNull(authentication, "authentication must not be null");

        if (authentication instanceof UsernamePasswordAuthenticationToken token) {
            try {
                final UserResult result = gitHubClient.currentUserViaToken(token);
                final GitHubUserDetails userDetails = createUserDetails(result);
                return createAuthentication(token, userDetails);
            } catch (GitHubHttpStatusException hse) {
                if (HttpStatus.UNAUTHORIZED.value() == hse.getHttpStatus()) {
                    throw new BadCredentialsException("invalid login", hse);
                }
                throw hse;
            } catch (RuntimeException re) {
                logger.error("error authenticating via token", re);
            }
        }

        throw new IllegalArgumentException("unsupported authentication type " + authentication);
    }

    private static GitHubUserDetails createUserDetails(UserResult result) {
        final User user = result.getUser();
        final Set<SimpleGrantedAuthority> authorities = result.getScopes().stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());

        return new GitHubUserDetails(
            user,
            result.getAccessToken(),
            authorities,
            result.getTokenExpiry()
        );
    }

    private Authentication createAuthentication(UsernamePasswordAuthenticationToken token,
                                                GitHubUserDetails userDetails) {
        final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(token.getPrincipal(),
            token.getCredentials(),
            userDetails.getAuthorities());
        authentication.setDetails(userDetails);
        return authentication;
    }

}

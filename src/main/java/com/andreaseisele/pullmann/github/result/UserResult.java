package com.andreaseisele.pullmann.github.result;

import static java.util.function.Predicate.not;


import com.andreaseisele.pullmann.github.dto.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link UserResult} wraps a GitHub {@link User} with additional data.
 */
public class UserResult {

    private static final Logger logger = LoggerFactory.getLogger(UserResult.class);

    public static final DateTimeFormatter EXPIRATION_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    private final User user;
    private final Set<String> scopes;
    private final LocalDateTime tokenExpiry;

    private UserResult(User user, Set<String> scopes, LocalDateTime tokenExpiry) {
        this.user = user;
        this.scopes = scopes;
        this.tokenExpiry = tokenExpiry;
    }

    public static UserResult of(User user, String scopeList, String tokenExpiry) {
        final var expiry = parseExpiry(tokenExpiry);
        final var scopes = parseScopeList(scopeList);
        return new UserResult(user, scopes, expiry);
    }

    static LocalDateTime parseExpiry(String tokenExpiry) {
        try {
            return LocalDateTime.parse(tokenExpiry, EXPIRATION_FORMATTER);
        } catch (DateTimeParseException dtpe) {
            logger.warn("error parsing token expiry [{}]", tokenExpiry, dtpe);
            return null;
        }
    }

    static Set<String> parseScopeList(String scopeList) {
        if (scopeList == null) {
            logger.warn("list of scopes is null");
            return Collections.emptySet();
        }
        return Arrays.stream(scopeList.split(","))
            .map(String::trim)
            .filter(not(String::isBlank))
            .collect(Collectors.toSet());
    }

    public User getUser() {
        return user;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public LocalDateTime getTokenExpiry() {
        return tokenExpiry;
    }

}

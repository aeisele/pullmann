package com.andreaseisele.pullmann.domain;

import static java.util.Objects.requireNonNull;


import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record RepositoryName(String owner, String repository) {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryName.class);

    public static final String REGEX_REPO_FULL_NAME = "(?<owner>[\\w,\\d,\\-,_,\\.]+)\\/(?<repo>[\\w,\\d,\\-,_,\\.]+)";
    private static final Pattern PATTERN_REPO_FULL_NAME = Pattern.compile(REGEX_REPO_FULL_NAME);

    public RepositoryName {
        requireNonNull(owner, "owner must not be null");
        requireNonNull(repository, "repository must not be null");
    }

    /**
     * Attempt to parse a full name expression like "octocat/Hello-World".
     *
     * @param fullName the full name
     * @return the parse result if successful
     */
    public static Optional<RepositoryName> parse(String fullName) {
        final Matcher matcher = PATTERN_REPO_FULL_NAME.matcher(fullName.trim());
        if (matcher.matches()) {
            return Optional.of(new RepositoryName(matcher.group("owner"), matcher.group("repo")));
        }

        logger.warn("unable to parse repo full name [{}]", fullName);
        return Optional.empty();
    }

}

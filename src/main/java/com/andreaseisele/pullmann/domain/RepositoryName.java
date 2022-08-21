package com.andreaseisele.pullmann.domain;

import java.util.Optional;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryName {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryName.class);

    public static final String REGEX_REPO_FULL_NAME = "(?<owner>[\\w,\\d,\\-,_,\\.]+)\\/(?<repo>[\\w,\\d,\\-,_,\\.]+)";
    private static final Pattern PATTERN_REPO_FULL_NAME = Pattern.compile(REGEX_REPO_FULL_NAME);

    private final String owner;
    private final String repository;

    public RepositoryName(String owner, String repository) {
        this.owner = owner;
        this.repository = repository;
    }

    /**
     * Attempt to parse a full name expression like "octocat/Hello-World".
     * @param fullName the full name
     * @return the parse result if successful
     */
    public static Optional<RepositoryName> parse(String fullName) {
        final var matcher = PATTERN_REPO_FULL_NAME.matcher(fullName.trim());
        if (matcher.matches()) {
            return Optional.of(new RepositoryName(matcher.group("owner"), matcher.group("repo")));
        }

        logger.warn("unable to parse repo full name [{}]", fullName);
        return Optional.empty();
    }

    public String getOwner() {
        return owner;
    }

    public String getRepository() {
        return repository;
    }

}

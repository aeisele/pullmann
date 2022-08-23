package com.andreaseisele.pullmann.domain;

import static java.util.Objects.requireNonNull;

public record PullRequestCoordinates(
    RepositoryName repositoryName,
    long number
) {

    public PullRequestCoordinates {
        requireNonNull(repositoryName, "repositoryName must not be null");
    }
}

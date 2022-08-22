package com.andreaseisele.pullmann.domain;

public record PullRequestCoordinates(
    RepositoryName repositoryName,
    long number
) {
}

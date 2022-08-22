package com.andreaseisele.pullmann.github.dto;

public record UpdateRequest(
    PullRequest.State state
) {
}

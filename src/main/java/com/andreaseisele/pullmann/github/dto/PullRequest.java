package com.andreaseisele.pullmann.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * GitHub Pull Request DTO.
 * See <a href="https://docs.github.com/en/rest/pulls/pulls">Documentation</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PullRequest(
    Long id,
    Long number,
    String url,
    String title,
    String body,
    State state,
    User user,
    BranchInfo head,
    BranchInfo base
) {
    public enum State {
        OPEN,
        CLOSED
    }
}

package com.andreaseisele.pullmann.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MergeRequest(
    @JsonProperty("commit_message") String commitMessage,
    String sha
) {
}

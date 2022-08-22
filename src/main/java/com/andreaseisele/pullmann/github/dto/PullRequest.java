package com.andreaseisele.pullmann.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

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
    BranchInfo base,
    @JsonProperty("changed_files") Long changedFiles,
    Boolean mergeable,
    Boolean merged
) {
    public enum State {
        OPEN("open"),
        CLOSED("closed");

        private final String apiValue;

        State(String apiValue) {
            this.apiValue = apiValue;
        }

        @JsonValue
        public String getApiValue() {
            return apiValue;
        }

    }
}

package com.andreaseisele.pullmann.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GitHub User DTO.
 * See <a href="https://docs.github.com/en/rest/users/users">Documentation</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record User(
    Long id,
    String login,
    String name,
    String email,
    @JsonProperty("avatar_url") String avatarUrl,
    @JsonProperty("repos_url") String reposUrl
) {
}

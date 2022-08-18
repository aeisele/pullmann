package com.andreaseisele.pullmann.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GitHub User DTO.
 * See <a href="https://docs.github.com/en/rest/users/users">Documentation</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
// naming strategy doesn't work with records currently...
//@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record User(
    Long id,
    String login,
    String name,
    String email,
    @JsonProperty("avatar_url") String avatarUrl,
    @JsonProperty("repos_url") String reposUrl
) {
}

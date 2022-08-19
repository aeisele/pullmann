package com.andreaseisele.pullmann.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GitHub Repository DTO.
 * See <a href="https://docs.github.com/en/rest/repos/repos#list-repositories-for-the-authenticated-user">Documentation</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Repository(
    Long id,
    String name,
    @JsonProperty("full_name") String fullName,
    Integer size
) {
}

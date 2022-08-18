package com.andreaseisele.pullmann.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ErrorMessage(
    String message,
    @JsonProperty("documentation_url") String documentationUrl
) {
}

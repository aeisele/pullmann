package com.andreaseisele.pullmann.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GitHub DTO for a file.
 * See <a href="https://docs.github.com/en/rest/pulls/pulls#list-pull-requests-files">Documentation</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record File(
    String sha,
    String filename,
    @JsonProperty("raw_url") String rawUrl,
    @JsonProperty("blob_url") String blobUrl
) {
}

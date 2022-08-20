package com.andreaseisele.pullmann.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BranchInfo(
    String label,
    String ref,
    String sha
) {
}

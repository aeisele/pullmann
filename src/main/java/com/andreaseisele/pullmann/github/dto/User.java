package com.andreaseisele.pullmann.github.dto;

import java.net.URI;

public record User(
    Long id,
    String login,
    String name,
    String email,
    URI avatarUrl,
    URI reposUrl
) {
}

package com.andreaseisele.pullmann.github;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "pullman.github")
public record GitHubProperties(
    @URL @NotNull String baseUrl,
    @PositiveOrZero int connectTimeoutSeconds
) {
}

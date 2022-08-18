package com.andreaseisele.pullmann.github;

import com.andreaseisele.pullmann.github.error.GitHubInitException;
import okhttp3.HttpUrl;
import org.springframework.stereotype.Component;

@Component
public class GitHubUrls {

    private static final String PATH_USER = "user";

    private final GitHubProperties properties;

    public GitHubUrls(GitHubProperties properties) {
        this.properties = properties;
    }

    public HttpUrl currentUser() {
        return parseBaseUrl().resolve(PATH_USER);
    }

    private HttpUrl parseBaseUrl() {
        final var parsed = HttpUrl.parse(properties.getBaseUrl());
        if (parsed == null) {
            throw new GitHubInitException("unable to parse configured base URL");
        }
        return parsed;
    }

}

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
        return parseBaseUrl(properties.getBaseUrl());
    }

    static HttpUrl parseBaseUrl(String baseUrl) {
        final var parsed = HttpUrl.parse(baseUrl);
        if (parsed == null) {
            throw new GitHubInitException("unable to parse configured base URL " + baseUrl);
        }
        return parsed;
    }

}

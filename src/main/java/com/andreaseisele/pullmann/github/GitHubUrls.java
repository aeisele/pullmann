package com.andreaseisele.pullmann.github;

import com.andreaseisele.pullmann.github.error.GitHubInitException;
import okhttp3.HttpUrl;
import org.springframework.stereotype.Component;

@Component
public class GitHubUrls {

    private static final String PATH_USER = "user";
    private static final String PATH_USER_REPOS = "user/repos";
    private static final String PATH_PULL_REQUESTS = "repos/{owner}/{name}/pulls";

    private final GitHubProperties properties;

    public GitHubUrls(GitHubProperties properties) {
        this.properties = properties;
    }

    public HttpUrl currentUser() {
        return parseBaseUrl().resolve(PATH_USER);
    }

    public HttpUrl userRepos() {
        return parseBaseUrl().resolve(PATH_USER_REPOS);
    }

    public HttpUrl pullRequests() {
        return parseBaseUrl().resolve(PATH_PULL_REQUESTS);
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

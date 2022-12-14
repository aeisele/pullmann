package com.andreaseisele.pullmann.github;

import com.andreaseisele.pullmann.domain.PullRequestCoordinates;
import com.andreaseisele.pullmann.domain.RepositoryName;
import com.andreaseisele.pullmann.github.error.GitHubInitException;
import okhttp3.HttpUrl;
import org.springframework.stereotype.Component;

@Component
public class GitHubUrls {

    private static final String PATH_USER = "user";
    private static final String PATH_USER_REPOS = "user/repos";
    private static final String PATH_PULL_REQUESTS = "repos/{owner}/{repository}/pulls";
    private static final String PATH_PULL_REQUEST_DETAILS = PATH_PULL_REQUESTS + "/{pullNumber}";
    private static final String PATH_PULL_REQUEST_MERGE = PATH_PULL_REQUEST_DETAILS + "/merge";
    private static final String PATH_PULL_REQUEST_FILES = PATH_PULL_REQUEST_DETAILS + "/files";
    private static final String PATH_REPO_CONTENTS = "repos/{owner}/{repository}/zipball/{ref}";
    private static final String PATH_USER_REPO_PERMISSION = "repos/{owner}/{repository}/collaborators/{username}/permission";

    private static final String QUERY_PARAM_PAGE = "page";
    private static final String QUERY_PARAM_STATE = "state";
    private static final String QUERY_PARAM_PER_PAGE = "per_page";

    private final GitHubProperties properties;

    public GitHubUrls(GitHubProperties properties) {
        this.properties = properties;
    }

    public HttpUrl currentUser() {
        return parseBaseUrl().resolve(PATH_USER);
    }

    public HttpUrl userRepos(int page) {
        return builderFor(PATH_USER_REPOS)
            .setQueryParameter(QUERY_PARAM_PAGE, String.valueOf(page))
            .build();
    }

    public HttpUrl pullRequests(RepositoryName repositoryName, int page, String state) {
        return builderFor(PATH_PULL_REQUESTS)
            .setPathSegment(1, repositoryName.owner())
            .setPathSegment(2, repositoryName.repository())
            .setQueryParameter(QUERY_PARAM_PAGE, String.valueOf(page))
            .setQueryParameter(QUERY_PARAM_STATE, state)
            .build();
    }

    public HttpUrl pullRequestDetails(PullRequestCoordinates coordinates) {
        return builderFor(PATH_PULL_REQUEST_DETAILS)
            .setPathSegment(1, coordinates.repositoryName().owner())
            .setPathSegment(2, coordinates.repositoryName().repository())
            .setPathSegment(4, String.valueOf(coordinates.number()))
            .build();
    }

    public HttpUrl pullRequestMerge(PullRequestCoordinates coordinates) {
        return builderFor(PATH_PULL_REQUEST_MERGE)
            .setPathSegment(1, coordinates.repositoryName().owner())
            .setPathSegment(2, coordinates.repositoryName().repository())
            .setPathSegment(4, String.valueOf(coordinates.number()))
            .build();
    }

    public HttpUrl pullRequestFiles(PullRequestCoordinates coordinates, int page, int perPage) {
        return builderFor(PATH_PULL_REQUEST_FILES)
            .setPathSegment(1, coordinates.repositoryName().owner())
            .setPathSegment(2, coordinates.repositoryName().repository())
            .setPathSegment(4, String.valueOf(coordinates.number()))
            .setQueryParameter(QUERY_PARAM_PAGE, String.valueOf(page))
            .setQueryParameter(QUERY_PARAM_PER_PAGE, String.valueOf(perPage))
            .build();
    }

    public HttpUrl repositoryContents(RepositoryName repositoryName, String ref) {
        return builderFor(PATH_REPO_CONTENTS)
            .setPathSegment(1, repositoryName.owner())
            .setPathSegment(2, repositoryName.repository())
            .setPathSegment(4, ref)
            .build();
    }

    public HttpUrl userRepositoryPermission(RepositoryName repositoryName, String username) {
        return builderFor(PATH_USER_REPO_PERMISSION)
            .setPathSegment(1, repositoryName.owner())
            .setPathSegment(2, repositoryName.repository())
            .setPathSegment(4, username)
            .build();
    }

    private HttpUrl.Builder builderFor(String path) {
        final HttpUrl resolved = parseBaseUrl().resolve(path);
        if (resolved == null) {
            throw new GitHubInitException("unable to resolve base url against path " + path);
        }
        return resolved.newBuilder();
    }

    private HttpUrl parseBaseUrl() {
        return parseBaseUrl(properties.getBaseUrl());
    }

    static HttpUrl parseBaseUrl(String baseUrl) {
        final HttpUrl parsed = HttpUrl.parse(baseUrl);
        if (parsed == null) {
            throw new GitHubInitException("unable to parse configured base URL " + baseUrl);
        }
        return parsed;
    }
}

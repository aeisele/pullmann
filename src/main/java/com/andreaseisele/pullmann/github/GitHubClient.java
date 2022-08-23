package com.andreaseisele.pullmann.github;

import static java.util.Objects.requireNonNull;


import com.andreaseisele.pullmann.domain.PullRequestCoordinates;
import com.andreaseisele.pullmann.domain.RepositoryName;
import com.andreaseisele.pullmann.github.dto.ErrorMessage;
import com.andreaseisele.pullmann.github.dto.File;
import com.andreaseisele.pullmann.github.dto.MergeRequest;
import com.andreaseisele.pullmann.github.dto.MergeResponse;
import com.andreaseisele.pullmann.github.dto.PullRequest;
import com.andreaseisele.pullmann.github.dto.Repository;
import com.andreaseisele.pullmann.github.dto.UpdateRequest;
import com.andreaseisele.pullmann.github.dto.User;
import com.andreaseisele.pullmann.github.error.GitHubAuthenticationException;
import com.andreaseisele.pullmann.github.error.GitHubDownloadException;
import com.andreaseisele.pullmann.github.error.GitHubExecutionException;
import com.andreaseisele.pullmann.github.error.GitHubHttpStatusException;
import com.andreaseisele.pullmann.github.error.GitHubSerializationException;
import com.andreaseisele.pullmann.github.result.FileResult;
import com.andreaseisele.pullmann.github.result.MergeResult;
import com.andreaseisele.pullmann.github.result.PullRequestResult;
import com.andreaseisele.pullmann.github.result.UserResult;
import com.andreaseisele.pullmann.security.AuthenticationHolder;
import com.andreaseisele.pullmann.security.GitHubUserDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class GitHubClient {

    private static final Logger logger = LoggerFactory.getLogger(GitHubClient.class);

    private final OkHttpClient httpClient;
    private final GitHubUrls urls;
    private final ObjectMapper objectMapper;

    public GitHubClient(@Qualifier("githubHttpClient") OkHttpClient httpClient,
                        GitHubUrls urls,
                        @Qualifier("githubObjectMapper") ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.urls = urls;
        this.objectMapper = objectMapper;
    }

    /**
     * Authenticate via the given token and GET the current user.
     * @param token token holding username and personal access token
     * @return the resulting data, if successful
     * @throws com.andreaseisele.pullmann.github.error.GitHubException on any error
     */
    public UserResult currentUserViaToken(UsernamePasswordAuthenticationToken token) {
        final var credentials = buildCredentials(token);
        final var accessToken = (String) token.getCredentials();
        final var url = urls.currentUser();
        final var request = new Request.Builder()
            .url(url)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, credentials)
            .build();

        return executeCall("currentUserViaToken", request, response -> {
            final var user = unmarshall(response.body(), User.class);
            return UserResult.of(user,
                accessToken,
                response.header(GitHubHeaders.OAUTH_SCOPES),
                response.header(GitHubHeaders.TOKEN_EXPIRATION));
        });
    }

    public List<Repository> userRepos() {
        final var credentials = buildCredentialsFromCurrentAuth();
        final var url = urls.userRepos();
        final var request = new Request.Builder()
            .url(url)
            .header(HttpHeaders.ACCEPT, GitHubMediaTypes.JSON)
            .header(HttpHeaders.AUTHORIZATION, credentials)
            .build();

        return executeCall("userRepos",
            request,
            response -> unmarshallList(response.body(), Repository.class));
    }

    public PullRequestResult pullRequestsForRepo(RepositoryName repositoryName, int page) {
        final var credentials = buildCredentialsFromCurrentAuth();
        final var url = urls.pullRequests(repositoryName, page, "all");

        final var request = new Request.Builder()
            .url(url)
            .header(HttpHeaders.ACCEPT, GitHubMediaTypes.JSON)
            .header(HttpHeaders.AUTHORIZATION, credentials)
            .build();

        return executeCall("pullRequestsForRepo",
            request,
            response -> { // OK
                final var pullRequests = unmarshallList(response.body(), PullRequest.class);
                return PullRequestResult.of(pullRequests, page, response.header(HttpHeaders.LINK));
            },
            response -> { // BAD
                if (response.code() == HttpStatus.NOT_FOUND.value()) {
                    return PullRequestResult.empty();
                } else {
                    return GitHubClient.<PullRequestResult>defaultBadStatusHandler().apply(response);
                }
            });
    }

    public PullRequest pullRequestDetails(PullRequestCoordinates coordinates) {
        final var credentials = buildCredentialsFromCurrentAuth();
        final var url = urls.pullRequestDetails(coordinates);

        final var request = new Request.Builder()
            .url(url)
            .header(HttpHeaders.ACCEPT, GitHubMediaTypes.JSON)
            .header(HttpHeaders.AUTHORIZATION, credentials)
            .build();

        return executeCall("pullRequestDetails",
            request,
            response -> unmarshall(response.body(), PullRequest.class));
    }

    public MergeResult merge(PullRequestCoordinates coordinates, String message, String sha) {
        final var credentials = buildCredentialsFromCurrentAuth();
        final var url = urls.pullRequestMerge(coordinates);

        final var body = marshall(new MergeRequest(message, sha));

        final var request = new Request.Builder()
            .url(url)
            .put(RequestBody.create(body, okhttp3.MediaType.get(MediaType.APPLICATION_JSON_VALUE)))
            .header(HttpHeaders.ACCEPT, GitHubMediaTypes.JSON)
            .header(HttpHeaders.AUTHORIZATION, credentials)
            .build();

        return executeCall("merge",
            request,
            response -> MergeResult.of(unmarshall(response.body(), MergeResponse.class)), // OK
            response ->  // FAILURE
                switch (response.code()) {
                    case 404, 405, 409 -> MergeResult.failure();
                    default -> GitHubClient.<MergeResult>defaultBadStatusHandler().apply(response);
                });
    }

    public boolean close(PullRequestCoordinates coordinates) {
        final var credentials = buildCredentialsFromCurrentAuth();
        final var url = urls.pullRequestDetails(coordinates);

        final var body = marshall(new UpdateRequest(PullRequest.State.CLOSED));

        final var request = new Request.Builder()
            .url(url)
            .patch(RequestBody.create(body, okhttp3.MediaType.get(MediaType.APPLICATION_JSON_VALUE)))
            .header(HttpHeaders.ACCEPT, GitHubMediaTypes.JSON)
            .header(HttpHeaders.AUTHORIZATION, credentials)
            .build();

        return executeCall("close",
            request,
            response -> true, // OK
            response ->  // BAD
                switch (response.code()) {
                    case 403, 404, 422 -> false;
                    default -> GitHubClient.<Boolean>defaultBadStatusHandler().apply(response);
                });
    }

    public FileResult files(PullRequestCoordinates coordinates, int page) {
        final var credentials = buildCredentialsFromCurrentAuth();
        final var url = urls.pullRequestFiles(coordinates, page, 100);

        final var request = new Request.Builder()
            .url(url)
            .get()
            .header(HttpHeaders.ACCEPT, GitHubMediaTypes.JSON)
            .header(HttpHeaders.AUTHORIZATION, credentials)
            .build();

        return executeCall("files",
            request,
            response -> {
                final var files = unmarshallList(response.body(), File.class);
                return FileResult.of(files, page, response.header(HttpHeaders.LINK));
            });
    }

    /**
     * Downloads the response (body) to the given url directly.
     * @param url href to the download
     * @param target file to store the download to
     * @throws GitHubDownloadException when the download fails due to IO errors
     */
    public boolean downloadFile(String url, Path target) {
        final var credentials = buildCredentialsFromCurrentAuth();

        final var request = new Request.Builder()
            .url(url)
            .get()
            .header(HttpHeaders.AUTHORIZATION, credentials)
            .header(HttpHeaders.ACCEPT, GitHubMediaTypes.RAW)
            .build();

        return executeCall("download",
            request,
            response -> {
                downloadToTarget(response, target);
                return true;
            });
    }

    private <R> R executeCall(String callName, Request request, Function<Response, R> successHandler) {
        return executeCall(callName, request, successHandler, defaultBadStatusHandler());
    }

    private <R> R executeCall(String callName,
                              Request request,
                              Function<Response, R> successHandler,
                              Function<Response, R> badStatusHandler) {
        requireNonNull(successHandler, "success handler must not be null");
        requireNonNull(badStatusHandler, "bad status handler must not be null");

        try (final var response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return successHandler.apply(response);
            } else {
                logErrorResponse(callName, response);
                return badStatusHandler.apply(response);
            }
        } catch (IOException e) {
            throw new GitHubExecutionException("error on call '%s'".formatted(callName), e);
        }
    }

    private void logErrorResponse(String callName, Response errorResponse) {
        final var error = tryReadError(errorResponse.body());
        if (error != null) {
            logger.warn("call '{}' was not OK: status={}, message={}, documentation-url={}",
                callName,
                errorResponse.code(),
                error.message(),
                error.documentationUrl());
        } else {
            logger.warn("call 'currentUserViaToken' was not OK: status={}, no message", errorResponse.code());
        }
    }

    private static <R> Function<Response, R> defaultBadStatusHandler() {
        return response -> {
            throw new GitHubHttpStatusException(response.code(), "unexpected HTTP status code");
        };
    }

    private ErrorMessage tryReadError(ResponseBody body) {
        if (body == null) {
            return null;
        }
        return unmarshall(body, ErrorMessage.class);
    }

    private <T> T unmarshall(ResponseBody body, Class<T> type) {
        if (body == null) {
            throw new GitHubExecutionException("tried to unmarshall a null body");
        }
        try {
            return objectMapper.readValue(body.charStream(), type);
        } catch (IOException e) {
            throw new GitHubSerializationException("error unmarshalling to " + type, e);
        }
    }

    private <T> List<T> unmarshallList(ResponseBody body, Class<T> type) {
        if (body == null) {
            throw new GitHubExecutionException("tried to unmarshall a null body");
        }
        final var collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, type);
        try {
            return objectMapper.readValue(body.charStream(), collectionType);
        } catch (IOException e) {
            throw new GitHubSerializationException("error unmarshalling to " + collectionType, e);
        }
    }

    private <T> String marshall(T dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new GitHubSerializationException("error marshalling " + dto.getClass(), e);
        }
    }

    private void downloadToTarget(Response response, Path target) {
        final var body = response.body();
        if (body == null) {
            throw new GitHubExecutionException("tried to download a null body");
        }
        try (InputStream in = body.byteStream()) {
            Files.copy(in, target);
        } catch (IOException e) {
            throw new GitHubDownloadException("error downloading file to target " + target, e);
        }
    }

    private static String buildCredentialsFromCurrentAuth() {
        return buildCredentials(AuthenticationHolder.currentAuthentication());
    }

    private static String buildCredentials(UsernamePasswordAuthenticationToken token) {
        final var username = extractUsername(token);
        final var accessToken = extractAccessToken(token);

        if (username == null || accessToken == null) {
            throw new GitHubAuthenticationException("unsupported principal or credential type in security context");
        }

        return Credentials.basic(username, accessToken);
    }

    private static String extractUsername(UsernamePasswordAuthenticationToken token) {
        if (token.getPrincipal() instanceof String username) {
            return username;
        } else if (token.getPrincipal() instanceof org.springframework.security.core.userdetails.User user) {
            return user.getUsername();
        }

        logger.error("unable to extract username from Authentication [{}]", token);
        return null;
    }

    private static String extractAccessToken(UsernamePasswordAuthenticationToken token) {
        if (token.getCredentials() instanceof String accessToken) {
            return accessToken;
        } else if (token.getDetails() instanceof GitHubUserDetails details) {
            return details.getAccessToken();
        }

        logger.error("unable to extract access token from Authentication [{}]", token);
        return null;
    }

}

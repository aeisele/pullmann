package com.andreaseisele.pullmann.github;

import com.andreaseisele.pullmann.github.dto.ErrorMessage;
import com.andreaseisele.pullmann.github.dto.User;
import com.andreaseisele.pullmann.github.error.GitHubAuthenticationException;
import com.andreaseisele.pullmann.github.error.GitHubExecutionException;
import com.andreaseisele.pullmann.github.error.GitHubHttpStatusException;
import com.andreaseisele.pullmann.github.result.UserResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class GitHubClient {

    private static final Logger logger = LoggerFactory.getLogger(GitHubClient.class);

    private final OkHttpClient httpClient;
    private final GitHubProperties properties;
    private final GitHubUrls urls;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GitHubClient(OkHttpClient httpClient, GitHubProperties properties, GitHubUrls urls) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.urls = urls;
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

        try (final var response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                final var user = unmarshall(response.body(), User.class);
                return UserResult.of(user,
                    accessToken,
                    response.header(GitHubHeaders.OAUTH_SCOPES),
                    response.header(GitHubHeaders.TOKEN_EXPIRATION));
            } else {
                logErrorResponse("currentUserViaToken", response);
                throw new GitHubHttpStatusException(response.code(), "unexpected HTTP status code");
            }
        } catch (IOException e) {
            throw new GitHubExecutionException("error on call 'currentUserViaToken'", e);
        }
    }

    private void logErrorResponse(String callName, Response errorResponse) throws IOException {
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

    private ErrorMessage tryReadError(ResponseBody body) throws IOException {
        if (body == null) {
            return null;
        }
        return unmarshall(body, ErrorMessage.class);
    }

    private <T> T unmarshall(ResponseBody body, Class<T> type) throws IOException {
        if (body == null) {
            logger.warn("tried to unmarshall null body");
            return null;
        }
        return objectMapper.readValue(body.charStream(), type);
    }

    private <T> String marshall(T dto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(dto);
    }

    private static String buildCredentials(UsernamePasswordAuthenticationToken token) {
        if (token.getPrincipal() instanceof String username
            && token.getCredentials() instanceof String pat) {
            return Credentials.basic(username, pat);
        }

        throw new GitHubAuthenticationException("unsupported principal or credential type in security context");
    }

}

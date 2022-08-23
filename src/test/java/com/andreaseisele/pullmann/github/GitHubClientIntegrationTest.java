package com.andreaseisele.pullmann.github;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


import com.andreaseisele.pullmann.domain.PullRequestCoordinates;
import com.andreaseisele.pullmann.domain.RepositoryName;
import com.andreaseisele.pullmann.github.dto.PullRequest;
import com.andreaseisele.pullmann.github.error.GitHubHttpStatusException;
import com.andreaseisele.pullmann.github.result.UserResult;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("integration-test")
@WireMockTest
class GitHubClientIntegrationTest {

    @Autowired
    private GitHubClient gitHubClient;

    @Autowired
    private GitHubProperties properties;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        properties.setBaseUrl("http://localhost:" + wmRuntimeInfo.getHttpPort());
    }

    @Test
    void currentUserViaToken_ok() {
        final var username = "user";
        final var pat = "d404bfb5-465e-41f8-abe6-98137d84db16";
        final var token = new UsernamePasswordAuthenticationToken(username, pat);

        stubFor(get("/user")
            .withBasicAuth(username, pat)
            .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .withHeader(GitHubHeaders.OAUTH_SCOPES, "public_repo, read:user, repo:status, user:email")
                .withHeader(GitHubHeaders.TOKEN_EXPIRATION, expirationIn3Months())
                .withBodyFile("current_user_ok.json")
            )
        );

        final var result = gitHubClient.currentUserViaToken(token);
        assertThat(result).isNotNull();

        final var user = result.getUser();
        assertThat(user).isNotNull();
        assertThat(user.id()).isEqualTo(1234567);
        assertThat(user.login()).isEqualTo("testuser");
        assertThat(user.name()).isEqualTo("Test User");
        assertThat(user.email()).isEqualTo("user@email.local");
        assertThat(user.avatarUrl()).isNotBlank();
        assertThat(user.reposUrl()).isNotBlank();

        assertThat(result.getTokenExpiry()).isAfter(LocalDateTime.now());
        assertThat(result.getScopes()).containsOnly("public_repo", "read:user", "repo:status", "user:email");
    }

    @Test
    void currentUserViaToken_unauthorized() {
        final var username = "user";
        final var pat = "wrong";
        final var token = new UsernamePasswordAuthenticationToken(username, pat);

        stubFor(get("/user")
            .withBasicAuth(username, "wrong")
            .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .willReturn(aResponse()
                .withStatus(HttpStatus.UNAUTHORIZED.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .withBodyFile("current_user_unauthorized.json")
            )
        );

        assertThatThrownBy(() -> gitHubClient.currentUserViaToken(token))
            .isInstanceOf(GitHubHttpStatusException.class)
            .hasMessageContaining("unexpected HTTP status code")
            .hasFieldOrPropertyWithValue("httpStatus", 401);
    }

    @WithMockUser(username = "test_user", password = "test")
    @Test
    void userRepos_ok() {
        stubFor(get("/user/repos")
            .withBasicAuth("test_user", "test")
            .withHeader(HttpHeaders.ACCEPT, equalTo(GitHubMediaTypes.JSON))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("user_repos.json")
            )
        );

        final var repositories = gitHubClient.userRepos();

        assertThat(repositories)
            .hasSize(1)
            .anySatisfy(repository -> {
                assertThat(repository.id()).isEqualTo(1296269);
                assertThat(repository.name()).isEqualTo("Hello-World");
                assertThat(repository.fullName()).isEqualTo("octocat/Hello-World");
                assertThat(repository.size()).isEqualTo(108);
            });
    }

    @WithMockUser(username = "test_user", password = "test")
    @Test
    void pullRequestsForRepo_ok() {
        final var linkHeaderValue =
            "<http://localhost/repositories/1/pulls?page=2>; rel=\"next\", <http://localhost/repositories/1/pulls?page=11>; rel=\"last\"";

        final var repositoryName = new RepositoryName("octocat", "Hello-World");

        stubFor(get(urlPathEqualTo("/repos/octocat/Hello-World/pulls"))
            .withQueryParam("page", equalTo("1"))
            .withBasicAuth("test_user", "test")
            .withHeader(HttpHeaders.ACCEPT, equalTo(GitHubMediaTypes.JSON))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withHeader(HttpHeaders.LINK, linkHeaderValue)
                .withBodyFile("repo_pull_requests.json")
            )
        );

        final var result = gitHubClient.pullRequestsForRepo(repositoryName, 1);

        assertThat(result).isNotNull();
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getMaxPages()).isEqualTo(11);

        final var pullRequests = result.getPullRequests();
        assertThat(pullRequests)
            .hasSize(1)
            .anySatisfy(pr -> {
                assertThat(pr.id()).isEqualTo(1);
                assertThat(pr.url()).isEqualTo("https://api.github.com/repos/octocat/Hello-World/pulls/1347");
                assertThat(pr.title()).isEqualTo("Amazing new feature");
                assertThat(pr.body()).isEqualTo("Please pull these awesome changes in!");
                assertThat(pr.state()).isEqualTo(PullRequest.State.OPEN);
                assertThat(pr.user()).isNotNull();

                final var head = pr.head();
                assertThat(head.label()).isEqualTo("octocat:new-topic");
                assertThat(head.ref()).isEqualTo("new-topic");
                assertThat(head.sha()).isEqualTo("6dcb09b5b57875f334f61aebed695e2e4193db5e");

                final var base = pr.base();
                assertThat(base.label()).isEqualTo("octocat:master");
                assertThat(base.ref()).isEqualTo("master");
                assertThat(base.sha()).isEqualTo("6dcb09b5b57875f334f61aebed695e2e4193db5e");
            });
    }

    @WithMockUser(username = "test_user", password = "test")
    @Test
    void pullRequestsForRepo_notFound() {
        final var repositoryName = new RepositoryName("octocat", "Hello-World");

        stubFor(get(urlPathEqualTo("/repos/octocat/Hello-World/pulls"))
            .withQueryParam("page", equalTo("1"))
            .withBasicAuth("test_user", "test")
            .withHeader(HttpHeaders.ACCEPT, equalTo(GitHubMediaTypes.JSON))
            .willReturn(aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("not_found.json")
            )
        );

        final var result = gitHubClient.pullRequestsForRepo(repositoryName, 1);

        assertThat(result).isNotNull();
        assertThat(result.getPullRequests()).isEmpty();
    }

    @WithMockUser(username = "test_user", password = "test")
    @Test
    void pullRequestDetails_ok() {
        final var repositoryName = new RepositoryName("octocat", "Hello-World");
        final var coordinates = new PullRequestCoordinates(repositoryName, 1347);

        stubFor(get(urlPathEqualTo("/repos/octocat/Hello-World/pulls/1347"))
            .withBasicAuth("test_user", "test")
            .withHeader(HttpHeaders.ACCEPT, equalTo(GitHubMediaTypes.JSON))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("pull_request_details.json")
            )
        );

        final var pullRequest = gitHubClient.pullRequestDetails(coordinates);

        assertThat(pullRequest).isNotNull();
        assertThat(pullRequest.id()).isEqualTo(1);
        assertThat(pullRequest.url()).isEqualTo("https://api.github.com/repos/octocat/Hello-World/pulls/1347");
        assertThat(pullRequest.title()).isEqualTo("Amazing new feature");
        assertThat(pullRequest.state()).isEqualTo(PullRequest.State.OPEN);
        assertThat(pullRequest.user()).isNotNull();
        assertThat(pullRequest.head()).isNotNull();
        assertThat(pullRequest.base()).isNotNull();
    }

    @WithMockUser(username = "test_user", password = "test")
    @Test
    void merge_ok() {
        final var repositoryName = new RepositoryName("octocat", "Hello-World");
        final var coordinates = new PullRequestCoordinates(repositoryName, 1347);
        final var message = "test message";
        final var sha = "6dcb09b5b57875f334f61aebed695e2e4193db5e";

        stubFor(put(urlPathEqualTo("/repos/octocat/Hello-World/pulls/1347/merge"))
            .withBasicAuth("test_user", "test")
            .withHeader(HttpHeaders.ACCEPT, equalTo(GitHubMediaTypes.JSON))
            .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.APPLICATION_JSON_VALUE))
            .withRequestBody(equalToJson("""
                {
                  "commit_message" : "test message",
                  "sha" : "6dcb09b5b57875f334f61aebed695e2e4193db5e"
                }"""))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("merge_success.json")
            )
        );

        final var result = gitHubClient.merge(coordinates, message, sha);

        assertThat(result).isNotNull();
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getResponse()).isNotNull();

        final var response = result.getResponse();
        assertThat(response.merged()).isTrue();
        assertThat(response.sha()).isEqualTo("6dcb09b5b57875f334f61aebed695e2e4193db5e");
        assertThat(response.message()).isEqualTo("Pull Request successfully merged");
    }

    @WithMockUser(username = "test_user", password = "test")
    @ValueSource(ints = {404, 405, 409})
    @ParameterizedTest
    void merge_failure(int badStatus) {
        final var repositoryName = new RepositoryName("octocat", "Hello-World");
        final var coordinates = new PullRequestCoordinates(repositoryName, 1347);
        final var message = "test message";
        final var sha = "6dcb09b5b57875f334f61aebed695e2e4193db5e";

        stubFor(put(urlPathEqualTo("/repos/octocat/Hello-World/pulls/1347/merge"))
            .withBasicAuth("test_user", "test")
            .withHeader(HttpHeaders.ACCEPT, equalTo(GitHubMediaTypes.JSON))
            .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.APPLICATION_JSON_VALUE))
            .withRequestBody(equalToJson("""
                {
                  "commit_message" : "test message",
                  "sha" : "6dcb09b5b57875f334f61aebed695e2e4193db5e"
                }"""))
            .willReturn(aResponse()
                .withStatus(badStatus)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("merge_failure.json")
            )
        );

        final var result = gitHubClient.merge(coordinates, message, sha);

        assertThat(result).isNotNull();
        assertThat(result.isSuccessful()).isFalse();
    }

    @WithMockUser(username = "test_user", password = "test")
    @Test
    void close_ok() {
        final var repositoryName = new RepositoryName("octocat", "Hello-World");
        final var coordinates = new PullRequestCoordinates(repositoryName, 1347);

        stubFor(patch(urlPathEqualTo("/repos/octocat/Hello-World/pulls/1347"))
            .withBasicAuth("test_user", "test")
            .withHeader(HttpHeaders.ACCEPT, equalTo(GitHubMediaTypes.JSON))
            .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.APPLICATION_JSON_VALUE))
            .withRequestBody(equalToJson("""
                {
                  "state" : "closed"
                }"""))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("close_pull_request_ok.json")
            )
        );

        final var success = gitHubClient.close(coordinates);

        assertThat(success).isTrue();
    }

    @WithMockUser(username = "test_user", password = "test")
    @ParameterizedTest
    @ValueSource(ints = {403, 404, 422})
    void close_failure(int badStatus) {
        final var repositoryName = new RepositoryName("octocat", "Hello-World");
        final var coordinates = new PullRequestCoordinates(repositoryName, 1347);

        stubFor(patch(urlPathEqualTo("/repos/octocat/Hello-World/pulls/1347"))
            .withBasicAuth("test_user", "test")
            .withHeader(HttpHeaders.ACCEPT, equalTo(GitHubMediaTypes.JSON))
            .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.APPLICATION_JSON_VALUE))
            .withRequestBody(equalToJson("""
                {
                  "state" : "closed"
                }"""))
            .willReturn(aResponse()
                .withStatus(badStatus)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("close_pull_request_failure.json")
            )
        );

        final var success = gitHubClient.close(coordinates);

        assertThat(success).isFalse();
    }

    @WithMockUser(username = "test_user", password = "test")
    @Test
    void files_ok() {
        final var linkHeaderValue =
            "<http://localhost/repositories/1/pulls/1347/files?page=2>; rel=\"next\", <http://localhost/repositories/1/pulls/1347/?page=11>; rel=\"last\"";

        final var repositoryName = new RepositoryName("octocat", "Hello-World");
        final var coordinates = new PullRequestCoordinates(repositoryName, 1347);

        stubFor(get(urlPathEqualTo("/repos/octocat/Hello-World/pulls/1347/files"))
            .withBasicAuth("test_user", "test")
            .withHeader(HttpHeaders.ACCEPT, equalTo(GitHubMediaTypes.JSON))
            .withQueryParam("page", equalTo("1"))
            .withQueryParam("per_page", equalTo("100"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withHeader(HttpHeaders.LINK, linkHeaderValue)
                .withBodyFile("pull_request_files.json")
            )
        );

        final var result = gitHubClient.files(coordinates, 1);

        assertThat(result).isNotNull();
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getMaxPages()).isEqualTo(11);
        assertThat(result.getFiles())
            .hasSize(1)
            .anySatisfy(file -> {
                assertThat(file.sha()).isEqualTo("bbcd538c8e72b8c175046e27cc8f907076331401");
                assertThat(file.filename()).isEqualTo("file1.txt");
                assertThat(file.rawUrl()).isEqualTo(
                    "https://github.com/octocat/Hello-World/blob/6dcb09b5b57875f334f61aebed695e2e4193db5e/file1.txt");
                assertThat(file.blobUrl()).isEqualTo(
                    "https://github.com/octocat/Hello-World/raw/6dcb09b5b57875f334f61aebed695e2e4193db5e/file1.txt");
            });
    }

    @WithMockUser(username = "test_user", password = "test")
    @Test
    void downloadFile_ok(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
        final var path = "/octocat/Hello-World/blob/6dcb09b5b57875f334f61aebed695e2e4193db5e/file1.txt";
        final var downloadUrl = wmRuntimeInfo.getHttpBaseUrl() + path;
        final var fileContent = new byte[2048];
        ThreadLocalRandom.current().nextBytes(fileContent);

        stubFor(get(urlPathEqualTo(path))
            .withBasicAuth("test_user", "test")
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withBody(fileContent)
            ));

        try (final var fs = Jimfs.newFileSystem(Configuration.unix())) {
            final var directory = fs.getPath("dir");
            Files.createDirectories(directory);

            final var target = directory.resolve("file1.txt");

            final var success = gitHubClient.downloadFile(downloadUrl, target);
            assertThat(success).isTrue();

            final var contentDownloaded = Files.readAllBytes(target);
            assertThat(contentDownloaded).isEqualTo(fileContent);
        }
    }

    private String expirationIn3Months() {
        final var dateTime = ZonedDateTime.of(LocalDateTime.now().plusMonths(3), ZoneId.of("UTC"));
        return UserResult.EXPIRATION_FORMATTER.format(dateTime);
    }
}
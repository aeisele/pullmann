package com.andreaseisele.pullmann.github;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;


import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("integration-test")
@WireMockTest
class GitHubClientIntegrationTest {

    private static final DateTimeFormatter EXPIRATION_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z");

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

        final var user = gitHubClient.currentUserViaToken(token);

        assertThat(user).isNotNull();
        assertThat(user.id()).isEqualTo(1234567);
        assertThat(user.login()).isEqualTo("testuser");
        assertThat(user.name()).isEqualTo("Test User");
        assertThat(user.email()).isEqualTo("user@email.local");
        assertThat(user.avatarUrl()).isNotBlank();
        assertThat(user.reposUrl()).isNotBlank();
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

        final var user = gitHubClient.currentUserViaToken(token);

        assertThat(user).isNull();
    }

    private String expirationIn3Months() {
        final var dateTime = ZonedDateTime.of(LocalDateTime.now().plusMonths(3), ZoneId.of("UTC"));
        return EXPIRATION_FORMATTER.format(dateTime);
    }
}
package com.andreaseisele.pullmann.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


import com.andreaseisele.pullmann.github.error.GitHubInitException;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;

class GitHubUrlsTest {

    @Test
    void parseBaseUrl_ok() {
        final String url = "http://localhost:8080";

        final HttpUrl parsed = GitHubUrls.parseBaseUrl(url);

        assertThat(parsed).isNotNull();
    }

    @Test
    void parseBaseUrl_invalid() {
        final String url = "htp://localhost:8080";

        assertThatThrownBy(() -> GitHubUrls.parseBaseUrl(url))
            .isInstanceOf(GitHubInitException.class)
            .hasMessageContaining("unable to parse configured base URL");
    }

}
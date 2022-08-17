package com.andreaseisele.pullmann.config;

import com.andreaseisele.pullmann.github.GitHubProperties;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpConfig {

    @Bean
    public HttpClient githubHttpClient(GitHubProperties gitHubProperties) {
        return HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(gitHubProperties.connectTimeoutSeconds()))
            .build();
    }

}

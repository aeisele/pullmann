package com.andreaseisele.pullmann.config;

import com.andreaseisele.pullmann.github.GitHubProperties;
import java.time.Duration;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class HttpConfig {

    private final GitHubProperties gitHubProperties;

    public HttpConfig(GitHubProperties gitHubProperties) {
        this.gitHubProperties = gitHubProperties;
    }

    @Bean
    public Interceptor loggingInterceptor() {
        var interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(gitHubProperties.getLogLevel());
        interceptor.redactHeader(HttpHeaders.AUTHORIZATION);
        interceptor.redactHeader(HttpHeaders.COOKIE);
        return interceptor;
    }

    @Bean
    public OkHttpClient githubHttpClient() {
        final var  timeouts = gitHubProperties.getTimeouts();

        return new OkHttpClient.Builder()
            .followRedirects(true)
            .connectTimeout(Duration.ofSeconds(timeouts.getConnectSeconds()))
            .writeTimeout(Duration.ofSeconds(timeouts.getWriteSeconds()))
            .readTimeout(Duration.ofSeconds(timeouts.getReadSeconds()))
            .callTimeout(Duration.ofSeconds(timeouts.getCallSeconds()))
            .addInterceptor(loggingInterceptor())
            .build();
    }

}

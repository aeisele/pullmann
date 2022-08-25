package com.andreaseisele.pullmann.config;

import com.andreaseisele.pullmann.github.GitHubProperties;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(gitHubProperties.getLogLevel());
        interceptor.redactHeader(HttpHeaders.AUTHORIZATION);
        interceptor.redactHeader(HttpHeaders.COOKIE);
        return interceptor;
    }

    @Bean
    public OkHttpClient githubHttpClient() {
        final GitHubProperties.Timeouts timeouts = gitHubProperties.getTimeouts();

        return new OkHttpClient.Builder()
            .followRedirects(true)
            .connectTimeout(Duration.ofSeconds(timeouts.getConnectSeconds()))
            .writeTimeout(Duration.ofSeconds(timeouts.getWriteSeconds()))
            .readTimeout(Duration.ofSeconds(timeouts.getReadSeconds()))
            .callTimeout(Duration.ofSeconds(timeouts.getCallSeconds()))
            .addInterceptor(loggingInterceptor())
            .build();
    }

    @Bean
    public ObjectMapper githubObjectMapper() {
        return JsonMapper.builder()
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .build();
    }

}

package com.andreaseisele.pullmann.config;

import com.andreaseisele.pullmann.github.GitHubProperties;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@Configuration
public class DownloadConfig {

    private final GitHubProperties gitHubProperties;

    public DownloadConfig(GitHubProperties gitHubProperties) {
        this.gitHubProperties = gitHubProperties;
    }

    @Bean
    public AsyncTaskExecutor pullRequestDownloadExecutor(TaskExecutorBuilder builder) {
        final var executor = builder.threadNamePrefix("downloader")
            .corePoolSize(gitHubProperties.getDownload().getMaxSimultaneous())
            .build();
        executor.initialize();

        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }

}

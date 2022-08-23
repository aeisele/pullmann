package com.andreaseisele.pullmann.service;

import com.andreaseisele.pullmann.github.GitHubClient;
import com.andreaseisele.pullmann.github.dto.PullRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {

    private final GitHubClient gitHubClient;
    private final AsyncTaskExecutor executor;

    public DownloadService(GitHubClient gitHubClient,
                           @Qualifier("downloadExecutor") AsyncTaskExecutor executor) {
        this.gitHubClient = gitHubClient;
        this.executor = executor;
    }

    public void startDownload(PullRequest pullRequest) {
        return; // TODO
    }

}

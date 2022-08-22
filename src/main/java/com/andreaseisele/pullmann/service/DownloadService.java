package com.andreaseisele.pullmann.service;

import com.andreaseisele.pullmann.github.GitHubClient;
import com.andreaseisele.pullmann.github.dto.PullRequest;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {

    private final GitHubClient gitHubClient;

    public DownloadService(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    public void startDownload(PullRequest pullRequest) {
        return; // TODO
    }

}

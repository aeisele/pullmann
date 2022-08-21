package com.andreaseisele.pullmann.service;

import com.andreaseisele.pullmann.domain.RepositoryName;
import com.andreaseisele.pullmann.github.GitHubClient;
import com.andreaseisele.pullmann.github.result.PullRequestResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class PullRequestService {

    private final GitHubClient gitHubClient;

    public PullRequestService(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    @PreAuthorize("isAuthenticated()")
    public PullRequestResult requestsForRepo(RepositoryName repositoryName, int page) {
        return gitHubClient.pullRequestsForRepo(repositoryName, page);
    }

}

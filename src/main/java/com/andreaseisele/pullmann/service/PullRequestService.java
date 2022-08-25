package com.andreaseisele.pullmann.service;

import com.andreaseisele.pullmann.domain.PullRequestCoordinates;
import com.andreaseisele.pullmann.domain.RepositoryName;
import com.andreaseisele.pullmann.github.GitHubClient;
import com.andreaseisele.pullmann.github.GitHubProperties;
import com.andreaseisele.pullmann.github.dto.PullRequest;
import com.andreaseisele.pullmann.github.result.MergeResult;
import com.andreaseisele.pullmann.github.result.PullRequestResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class PullRequestService {

    private final GitHubClient gitHubClient;

    private final GitHubProperties gitHubProperties;

    private final DownloadService downloadService;

    public PullRequestService(GitHubClient gitHubClient,
                              GitHubProperties gitHubProperties,
                              DownloadService downloadService) {
        this.gitHubClient = gitHubClient;
        this.gitHubProperties = gitHubProperties;
        this.downloadService = downloadService;
    }

    @PreAuthorize("isAuthenticated()")
    public PullRequestResult requestsForRepo(RepositoryName repositoryName, int page) {
        return gitHubClient.pullRequestsForRepo(repositoryName, page);
    }

    @PreAuthorize("isAuthenticated()")
    public PullRequest requestDetails(PullRequestCoordinates coordinates) {
        return gitHubClient.pullRequestDetails(coordinates);
    }

    @PreAuthorize("isAuthenticated()")
    public MergeResult merge(PullRequestCoordinates coordinates) {
        final PullRequest pullRequest = gitHubClient.pullRequestDetails(coordinates);
        return gitHubClient.merge(coordinates, gitHubProperties.getMergeMessage(), pullRequest.head().sha());
    }

    @PreAuthorize("isAuthenticated()")
    public boolean close(PullRequestCoordinates coordinates) {
        return gitHubClient.close(coordinates);
    }

    @PreAuthorize("isAuthenticated()")
    public void startDownload(PullRequestCoordinates coordinates){
        downloadService.startDownload(coordinates);
    }

}

package com.andreaseisele.pullmann.service;

import com.andreaseisele.pullmann.domain.RepositoryName;
import com.andreaseisele.pullmann.github.GitHubClient;
import com.andreaseisele.pullmann.github.dto.RepositoryPermission;
import com.andreaseisele.pullmann.github.result.RepositoryResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class RepositoryService {

    private final GitHubClient gitHubClient;

    public RepositoryService(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    @PreAuthorize("isAuthenticated()")
    public RepositoryResult listRepositories(int page) {
        return gitHubClient.userRepos(page);
    }

    @PreAuthorize("isAuthenticated()")
    public RepositoryPermission permissionForRepository(RepositoryName repositoryName) {
        return gitHubClient.usersRepositoryPermission(repositoryName);
    }

}

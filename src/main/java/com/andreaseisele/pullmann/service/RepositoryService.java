package com.andreaseisele.pullmann.service;

import com.andreaseisele.pullmann.github.GitHubClient;
import com.andreaseisele.pullmann.github.dto.Repository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class RepositoryService {

    private final GitHubClient gitHubClient;

    public RepositoryService(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    @PreAuthorize("isAuthenticated()")
    public List<Repository> listRepositories() {
        return gitHubClient.userRepos();
    }

}

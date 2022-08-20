package com.andreaseisele.pullmann.service;

import com.andreaseisele.pullmann.github.dto.PullRequest;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PullRequestService {

    public List<PullRequest> requestsForRepo(String repositoryFullName, int page) {
        // TODO
        return Collections.emptyList();
    }

}

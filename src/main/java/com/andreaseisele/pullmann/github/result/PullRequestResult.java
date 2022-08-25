package com.andreaseisele.pullmann.github.result;

import com.andreaseisele.pullmann.github.LinkParser;
import com.andreaseisele.pullmann.github.dto.PullRequest;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PullRequestResult {

    private final List<PullRequest> pullRequests;
    private final int page;
    private final int maxPages;

    private PullRequestResult(List<PullRequest> pullRequests, int page, int maxPages) {
        this.pullRequests = pullRequests;
        this.page = page;
        this.maxPages = maxPages;
    }

    public static PullRequestResult of(List<PullRequest> pullRequests, int page, String linkInfo) {
        final Optional<Integer> maxPage = LinkParser.getLastPage(linkInfo);
        return new PullRequestResult(pullRequests, page, maxPage.orElse(page));
    }

    public static PullRequestResult empty() {
        return new PullRequestResult(Collections.emptyList(), 1, 1);
    }

    public List<PullRequest> getPullRequests() {
        return pullRequests;
    }

    public int getPage() {
        return page;
    }

    public int getMaxPages() {
        return maxPages;
    }

}

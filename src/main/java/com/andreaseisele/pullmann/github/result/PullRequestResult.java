package com.andreaseisele.pullmann.github.result;

import com.andreaseisele.pullmann.github.LinkParser;
import com.andreaseisele.pullmann.github.dto.PullRequest;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PullRequestResult extends PagedResult<PullRequest> {

    private PullRequestResult(List<PullRequest> list, int page, int maxPages) {
        super(list, page, maxPages);
    }

    public static PullRequestResult of(List<PullRequest> pullRequests, int page, String linkInfo) {
        final Optional<Integer> maxPage = LinkParser.getLastPage(linkInfo);
        return new PullRequestResult(pullRequests, page, maxPage.orElse(page));
    }

    public static PullRequestResult empty() {
        return new PullRequestResult(Collections.emptyList(), 1, 1);
    }

}

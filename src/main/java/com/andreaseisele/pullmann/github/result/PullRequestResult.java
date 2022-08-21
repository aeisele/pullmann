package com.andreaseisele.pullmann.github.result;

import com.andreaseisele.pullmann.github.LinkParser;
import com.andreaseisele.pullmann.github.dto.PullRequest;
import java.util.List;
import java.util.Optional;
import okhttp3.HttpUrl;

public class PullRequestResult {

    private final List<PullRequest> pullRequests;
    private int page;
    private int maxPages;

    private PullRequestResult(List<PullRequest> pullRequests, int page, int maxPages) {
        this.pullRequests = pullRequests;
        this.page = page;
        this.maxPages = maxPages;
    }

    public static PullRequestResult of(List<PullRequest> pullRequests, int page, String linkInfo) {
        final var maxPage = parseLinkInfo(linkInfo);
        return new PullRequestResult(pullRequests, page, maxPage.orElse(page));
    }

    // Link: <https://api.github.com/repositories/2325298/pulls?page=2>; rel="next", <https://api.github.com/repositories/2325298/pulls?page=11>; rel="last"
    static Optional<Integer> parseLinkInfo(String linkInfo) {
        if (linkInfo == null || linkInfo.isBlank()) {
            return Optional.empty();
        }
        return LinkParser.getLastRel(linkInfo)
            .map(url -> {
                final var parsed = HttpUrl.parse(url);
                if (parsed == null) {
                    return null;
                }
                return parsed.queryParameter("page");
            })
            .map(Integer::valueOf);
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

package com.andreaseisele.pullmann.github.result;

import com.andreaseisele.pullmann.github.LinkParser;
import com.andreaseisele.pullmann.github.dto.Repository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RepositoryResult extends PagedResult<Repository> {

    private RepositoryResult(List<Repository> list, int page, int maxPages) {
        super(list, page, maxPages);
    }

    public static RepositoryResult of(List<Repository> repositories, int page, String linkInfo) {
        final Optional<Integer> maxPage = LinkParser.getLastPage(linkInfo);
        return new RepositoryResult(repositories, page, maxPage.orElse(page));
    }

    public static RepositoryResult empty() {
        return new RepositoryResult(Collections.emptyList(), 1, 1);
    }

}

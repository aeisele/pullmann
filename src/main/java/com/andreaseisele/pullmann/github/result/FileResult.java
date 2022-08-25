package com.andreaseisele.pullmann.github.result;

import com.andreaseisele.pullmann.github.LinkParser;
import com.andreaseisele.pullmann.github.dto.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FileResult extends PagedResult<File> {

    private FileResult(List<File> files, int page, int maxPages) {
        super(files, page, maxPages);
    }

    public static FileResult of(List<File> files, int page, String linkInfo) {
        final Optional<Integer> maxPage = LinkParser.getLastPage(linkInfo);
        return new FileResult(files, page, maxPage.orElse(page));
    }

    public static FileResult empty() {
        return new FileResult(Collections.emptyList(), 1, 1);
    }

}

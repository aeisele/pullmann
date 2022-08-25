package com.andreaseisele.pullmann.github.result;

import com.andreaseisele.pullmann.github.LinkParser;
import com.andreaseisele.pullmann.github.dto.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FileResult {

    private final List<File> files;
    private final int page;
    private final int maxPages;

    private FileResult(List<File> files, int page, int maxPages) {
        this.files = files;
        this.page = page;
        this.maxPages = maxPages;
    }

    public static FileResult of(List<File> files, int page, String linkInfo) {
        final Optional<Integer> maxPage = LinkParser.getLastPage(linkInfo);
        return new FileResult(files, page, maxPage.orElse(page));
    }

    public static FileResult empty() {
        return new FileResult(Collections.emptyList(), 1, 1);
    }

    public List<File> getFiles() {
        return files;
    }

    public int getPage() {
        return page;
    }

    public int getMaxPages() {
        return maxPages;
    }

}

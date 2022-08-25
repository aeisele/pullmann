package com.andreaseisele.pullmann.github.result;

import java.util.List;

public abstract class PagedResult<T> {

    private final List<T> list;
    private final int page;
    private final int maxPages;

    protected PagedResult(List<T> list, int page, int maxPages) {
        this.list = list;
        this.page = page;
        this.maxPages = maxPages;
    }

    public List<T> getList() {
        return list;
    }

    public int getPage() {
        return page;
    }

    public int getMaxPages() {
        return maxPages;
    }

}

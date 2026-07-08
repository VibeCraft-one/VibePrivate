package com.vibeprivate.gui;

import java.util.List;

final class AdminGuiPage {
    static final int CONTENT_SIZE = 45;

    private final int page;
    private final int totalItems;

    AdminGuiPage(int requestedPage, int totalItems) {
        this.totalItems = Math.max(0, totalItems);
        this.page = Math.max(0, Math.min(requestedPage, maxPage(this.totalItems)));
    }

    int page() {
        return page;
    }

    int displayPage() {
        return page + 1;
    }

    int totalPages() {
        return maxPage(totalItems) + 1;
    }

    boolean hasPrevious() {
        return page > 0;
    }

    boolean hasNext() {
        return page < maxPage(totalItems);
    }

    <T> List<T> slice(List<T> items) {
        int from = Math.min(page * CONTENT_SIZE, items.size());
        int to = Math.min(from + CONTENT_SIZE, items.size());
        return items.subList(from, to);
    }

    private static int maxPage(int totalItems) {
        if (totalItems <= 0) {
            return 0;
        }

        return (totalItems - 1) / CONTENT_SIZE;
    }
}

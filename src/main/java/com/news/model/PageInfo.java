package com.news.model;

import java.util.List;

/**
 * Generic pagination wrapper for list results.
 */
public class PageInfo<T> {
    private List<T> items;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;

    public PageInfo() {}

    public PageInfo(List<T> items, int currentPage, int pageSize, long totalItems) {
        this.items = items;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
    }

    public List<T> getItems() { return items; }
    public void setItems(List<T> items) { this.items = items; }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getTotalItems() { return totalItems; }
    public void setTotalItems(long totalItems) { this.totalItems = totalItems; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public boolean isHasNext() { return currentPage < totalPages; }
    public boolean isHasPrev() { return currentPage > 1; }
}

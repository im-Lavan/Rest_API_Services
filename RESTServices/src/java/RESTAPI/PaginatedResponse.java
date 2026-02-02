/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package RESTAPI;

import java.util.List;

/**
  * Paginated Response Model - Generic wrapper for paginated API responses
 * 
 * This generic class wraps any type of list data with pagination metadata
 * Provides consistent pagination structure across all endpoints
 * 
 * Purpose:
 * - Implements standard pagination pattern for large datasets
 * - Provides clients with navigation metadata (page numbers, totals, etc.)
 * - Enables efficient data retrieval by loading data in chunks
 * 
 * Generic Type Parameter <T>:
 * - Can wrap any object type (items, ItemDistanceResponse, etc.)
 * - Example: PaginatedResponse<items> or PaginatedResponse<ItemDistanceResponse>
 * - Allows code reuse across different data types
 * 
 * Pagination Logic:
 * - Page numbers start at 1 (not 0) for user-friendliness
 * - hasNextPage = true if currentPage < totalPages
 * - hasPreviousPage = true if currentPage > 1
 * - These boolean flags help clients build navigation UI
 * 
 * Used By:
 * - GET /items - Paginated item listing (5 items per page)
 * - Any future endpoints requiring pagination
 * 
 * Benefits:
 * - Reduces payload size by limiting items per response
 * - Improves performance by avoiding full dataset transfers
 * - Provides clear navigation for users browsing items
 * - Scalable solution for growing item catalogs
 * 
 * @param <T> The type of objects being paginated
 * @author N1237155
 */
public class PaginatedResponse<T> {
    
    private List<T> items;
    private int currentPage;
    private int pageSize;
    private int totalItems;
    private int totalPages;
    private boolean hasNextPage;
    private boolean hasPreviousPage;
    
    public PaginatedResponse(){
    }
    
    public PaginatedResponse(List<T> items, int currentPage,int pageSize, int totalItems, 
                             int totalPages){
        
        this.items = items;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        
        // Auto-calculate navigation flags based on current position
        this.hasNextPage = currentPage < totalPages;
        this.hasPreviousPage = currentPage > 1;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }

    public void setHasPreviousPage(boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }
    
    
    
    
}

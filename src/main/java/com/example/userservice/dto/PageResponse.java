package com.example.userservice.dto;

import lombok.Data;

/**
 * 分页响应DTO
 */
@Data
public class PageResponse<T> {

    private java.util.List<T> content;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private Boolean first;
    private Boolean last;

    public PageResponse() {}

    public PageResponse(org.springframework.data.domain.Page<T> page) {
        this.content = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.first = page.isFirst();
        this.last = page.isLast();
    }
} 
package com.posterpro.api.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtil {

    private static final int MAX_SIZE = 100;

    private PaginationUtil() {
    }

    public static Pageable toPageable(int page, int size, Sort sort) {
        if (page < 0) {
            throw new IllegalArgumentException("page must not be negative");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be at least 1");
        }
        return PageRequest.of(page, Math.min(size, MAX_SIZE), sort);
    }
}

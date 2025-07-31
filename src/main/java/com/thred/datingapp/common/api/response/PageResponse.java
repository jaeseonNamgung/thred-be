package com.thred.datingapp.common.api.response;


import java.util.List;

public record PageResponse<T>(
        int pageSize,
        boolean isLastPage,
        List<T> contents
) {
    public static <T> PageResponse<T> of (int pageSize, boolean isLastPage, List<T> contents) {
        return new PageResponse<>(pageSize, isLastPage, contents);
    }
}

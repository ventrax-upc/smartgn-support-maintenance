package com.ventrax.support.interfaces.rest.controllers;

import org.springframework.http.ResponseEntity;

import java.util.List;

final class CollectionsPage {
    static <T> ResponseEntity<List<T>> of(List<T> records, int page, int size) {
        if (page < 0 || size < 1 || size > 100)
            throw new IllegalArgumentException("Invalid pagination");
        return ResponseEntity.ok()
                .header("X-Total-Count", Integer.toString(records.size()))
                .header("X-Page", Integer.toString(page))
                .header("X-Page-Size", Integer.toString(size))
                .body(records.stream().skip((long) page * size).limit(size).toList());
    }
}

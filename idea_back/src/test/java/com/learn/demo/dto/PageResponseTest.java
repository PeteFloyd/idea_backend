package com.learn.demo.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class PageResponseTest {

    @Test
    void ofBuildsPageResponseFromPage() {
        List<String> content = List.of("a", "b");
        Page<String> page = new PageImpl<>(content, PageRequest.of(1, 2), 5);

        PageResponse<String> response = PageResponse.of(page);
        assertEquals(content, response.getContent());
        assertEquals(1, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(5, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
    }

    @Test
    void dataMethodsAndEqualityWork() {
        PageResponse<String> response = new PageResponse<>();
        response.setContent(List.of("x"));
        response.setPage(0);
        response.setSize(1);
        response.setTotalElements(1);
        response.setTotalPages(1);

        PageResponse<String> other = PageResponse.<String>builder()
            .content(List.of("x"))
            .page(0)
            .size(1)
            .totalElements(1)
            .totalPages(1)
            .build();

        assertEquals(response, other);
        assertTrue(response.toString().contains("page=0"));
    }
}

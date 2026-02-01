package com.learn.demo.dto.idea;

import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdeaQueryRequest {
    @Builder.Default
    private int page = 0;

    @Builder.Default
    @Max(100)
    private int size = 20;

    @Builder.Default
    private String sort = "createdAt,desc";

    private String keyword;

    private String tag;

    private Long userId;
}

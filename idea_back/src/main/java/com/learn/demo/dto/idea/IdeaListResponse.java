package com.learn.demo.dto.idea;

import com.learn.demo.entity.Idea;
import com.learn.demo.entity.Tag;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdeaListResponse {
    private Long id;
    private String title;
    private String description;
    private List<String> images;
    private List<String> tags;
    private AuthorDto author;
    private Long likeCount;
    private Long commentCount;
    private boolean liked;
    private LocalDateTime createdAt;

    public static IdeaListResponse fromIdea(Idea idea, boolean liked) {
        if (idea == null) {
            return null;
        }
        List<String> images = idea.getImages() == null
            ? Collections.emptyList()
            : List.copyOf(idea.getImages());
        List<String> tags = idea.getTags() == null
            ? Collections.emptyList()
            : idea.getTags().stream()
                .map(Tag::getName)
                .filter(Objects::nonNull)
                .toList();
        String description = idea.getDescription();
        String snippet = description == null ? null
            : description.length() <= 200 ? description : description.substring(0, 200);

        return IdeaListResponse.builder()
            .id(idea.getId())
            .title(idea.getTitle())
            .description(snippet)
            .images(images)
            .tags(tags)
            .author(AuthorDto.fromUser(idea.getUser()))
            .likeCount(idea.getLikeCount())
            .commentCount(idea.getCommentCount())
            .liked(liked)
            .createdAt(idea.getCreatedAt())
            .build();
    }
}

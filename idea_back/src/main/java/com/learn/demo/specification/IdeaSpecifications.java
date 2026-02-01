package com.learn.demo.specification;

import com.learn.demo.dto.idea.IdeaQueryRequest;
import com.learn.demo.entity.Idea;
import com.learn.demo.entity.Tag;
import com.learn.demo.enums.IdeaStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class IdeaSpecifications {
    public static Specification<Idea> withKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String pattern = "%" + keyword.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
            cb.like(cb.lower(root.get("title")), pattern),
            cb.like(cb.lower(root.get("description")), pattern)
        );
    }

    public static Specification<Idea> withTag(String tagName) {
        if (tagName == null || tagName.isBlank()) return null;
        return (root, query, cb) -> {
            Join<Idea, Tag> tags = root.join("tags");
            return cb.equal(cb.lower(tags.get("name")), tagName.toLowerCase());
        };
    }

    public static Specification<Idea> withUserId(Long userId) {
        if (userId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Idea> excludeDeleted() {
        return (root, query, cb) -> cb.notEqual(root.get("status"), IdeaStatus.DELETED);
    }

    public static Specification<Idea> combine(IdeaQueryRequest request) {
        Specification<Idea> spec = Specification.where(excludeDeleted());
        Specification<Idea> keywordSpec = withKeyword(request.getKeyword());
        if (keywordSpec != null) {
            spec = spec.and(keywordSpec);
        }
        Specification<Idea> tagSpec = withTag(request.getTag());
        if (tagSpec != null) {
            spec = spec.and(tagSpec);
        }
        Specification<Idea> userSpec = withUserId(request.getUserId());
        if (userSpec != null) {
            spec = spec.and(userSpec);
        }
        return spec;
    }
}

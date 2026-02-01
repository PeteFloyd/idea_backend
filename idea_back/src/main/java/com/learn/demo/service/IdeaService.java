package com.learn.demo.service;

import com.learn.demo.dto.PageResponse;
import com.learn.demo.dto.idea.CreateIdeaRequest;
import com.learn.demo.dto.idea.IdeaDetailResponse;
import com.learn.demo.dto.idea.IdeaListResponse;
import com.learn.demo.dto.idea.IdeaQueryRequest;
import com.learn.demo.dto.idea.UpdateIdeaRequest;
import com.learn.demo.entity.Idea;
import com.learn.demo.entity.Like;
import com.learn.demo.entity.Tag;
import com.learn.demo.entity.User;
import com.learn.demo.enums.IdeaStatus;
import com.learn.demo.exception.BusinessException;
import com.learn.demo.repository.IdeaRepository;
import com.learn.demo.repository.LikeRepository;
import com.learn.demo.repository.TagRepository;
import com.learn.demo.repository.UserRepository;
import com.learn.demo.specification.IdeaSpecifications;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdeaService {
    private static final String IDEA_NOT_FOUND = "Idea not found";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String FORBIDDEN = "Forbidden";
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "likeCount");

    private final IdeaRepository ideaRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final LikeRepository likeRepository;

    public PageResponse<IdeaListResponse> listIdeas(IdeaQueryRequest request, Long currentUserId) {
        Specification<Idea> spec = IdeaSpecifications.combine(request);
        Pageable pageable = buildPageable(request);
        Page<Idea> page = ideaRepository.findAll(spec, pageable);

        List<Idea> ideas = page.getContent();
        List<Long> ideaIds = ideas.stream()
            .map(Idea::getId)
            .filter(Objects::nonNull)
            .toList();

        Set<Long> likedIdeaIds = resolveLikedIdeaIds(currentUserId, ideaIds);
        List<IdeaListResponse> content = ideas.stream()
            .map(idea -> IdeaListResponse.fromIdea(idea, likedIdeaIds.contains(idea.getId())))
            .toList();

        Page<IdeaListResponse> responsePage = new PageImpl<>(content, pageable, page.getTotalElements());
        return PageResponse.of(responsePage);
    }

    public IdeaDetailResponse getIdeaDetail(Long id, Long currentUserId) {
        Idea idea = ideaRepository.findById(id)
            .orElseThrow(() -> new BusinessException(404, IDEA_NOT_FOUND));
        if (idea.getStatus() == IdeaStatus.DELETED) {
            throw new BusinessException(404, IDEA_NOT_FOUND);
        }

        boolean liked = currentUserId != null && likeRepository.existsByUserIdAndIdeaId(currentUserId, id);
        return IdeaDetailResponse.fromIdea(idea, liked);
    }

    @Transactional
    public IdeaDetailResponse createIdea(CreateIdeaRequest request, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
            .orElseThrow(() -> new BusinessException(404, USER_NOT_FOUND));

        Idea idea = new Idea();
        idea.setUser(user);
        idea.setTitle(request.getTitle());
        idea.setDescription(request.getDescription());
        idea.setImages(request.getImages());
        idea.setStatus(IdeaStatus.ACTIVE);
        idea.setTags(syncTags(Collections.emptySet(), request.getTags()));

        Idea saved = ideaRepository.save(idea);
        return IdeaDetailResponse.fromIdea(saved, false);
    }

    @Transactional
    public IdeaDetailResponse updateIdea(Long id, UpdateIdeaRequest request, Long currentUserId) {
        Idea idea = ideaRepository.findById(id)
            .orElseThrow(() -> new BusinessException(404, IDEA_NOT_FOUND));
        if (idea.getStatus() == IdeaStatus.DELETED) {
            throw new BusinessException(404, IDEA_NOT_FOUND);
        }
        if (!Objects.equals(idea.getUser().getId(), currentUserId)) {
            throw new BusinessException(403, FORBIDDEN);
        }

        idea.setTitle(request.getTitle());
        idea.setDescription(request.getDescription());
        idea.setImages(request.getImages());
        idea.setTags(syncTags(idea.getTags(), request.getTags()));

        Idea saved = ideaRepository.save(idea);
        boolean liked = currentUserId != null && likeRepository.existsByUserIdAndIdeaId(currentUserId, id);
        return IdeaDetailResponse.fromIdea(saved, liked);
    }

    @Transactional
    public void deleteIdea(Long id, Long currentUserId, boolean isAdmin) {
        Idea idea = ideaRepository.findById(id)
            .orElseThrow(() -> new BusinessException(404, IDEA_NOT_FOUND));
        if (idea.getStatus() == IdeaStatus.DELETED) {
            throw new BusinessException(404, IDEA_NOT_FOUND);
        }
        if (!isAdmin && !Objects.equals(idea.getUser().getId(), currentUserId)) {
            throw new BusinessException(403, FORBIDDEN);
        }

        for (Tag tag : safeTagSet(idea.getTags())) {
            decrementUsage(tag);
            tagRepository.save(tag);
        }
        idea.setStatus(IdeaStatus.DELETED);
        ideaRepository.save(idea);
    }

    public PageResponse<IdeaListResponse> getCurrentUserIdeas(IdeaQueryRequest request, Long currentUserId) {
        request.setUserId(currentUserId);
        return listIdeas(request, currentUserId);
    }

    private Pageable buildPageable(IdeaQueryRequest request) {
        String sortValue = request.getSort();
        String sortField = "createdAt";
        Sort.Direction direction = Sort.Direction.DESC;

        if (sortValue != null && !sortValue.isBlank()) {
            String[] parts = sortValue.split(",");
            if (parts.length > 0 && !parts[0].isBlank()) {
                String candidate = parts[0].trim();
                if (ALLOWED_SORT_FIELDS.contains(candidate)) {
                    sortField = candidate;
                }
            }
            if (parts.length > 1 && !parts[1].isBlank()) {
                direction = "asc".equalsIgnoreCase(parts[1].trim())
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            }
        }

        return PageRequest.of(request.getPage(), request.getSize(), Sort.by(direction, sortField));
    }

    private Set<Long> resolveLikedIdeaIds(Long currentUserId, List<Long> ideaIds) {
        if (currentUserId == null || ideaIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<Like> likes = likeRepository.findByUserIdAndIdeaIdIn(currentUserId, ideaIds);
        return likes.stream()
            .map(like -> like.getIdea() == null ? null : like.getIdea().getId())
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private Set<Tag> syncTags(Set<Tag> currentTags, List<String> requestedTags) {
        Set<Tag> safeCurrent = safeTagSet(currentTags);
        Map<String, Tag> currentByLower = new HashMap<>();
        for (Tag tag : safeCurrent) {
            if (tag.getName() != null) {
                currentByLower.put(tag.getName().toLowerCase(), tag);
            }
        }

        List<String> normalized = normalizeTagNames(requestedTags);
        Set<String> normalizedLower = normalized.stream()
            .map(name -> name.toLowerCase())
            .collect(Collectors.toSet());

        for (Tag tag : safeCurrent) {
            String name = tag.getName();
            if (name != null && !normalizedLower.contains(name.toLowerCase())) {
                decrementUsage(tag);
                tagRepository.save(tag);
            }
        }

        Set<Tag> result = new HashSet<>();
        for (String name : normalized) {
            String lower = name.toLowerCase();
            Tag existing = currentByLower.get(lower);
            if (existing != null) {
                result.add(existing);
                continue;
            }
            Tag tag = tagRepository.findByNameIgnoreCase(name).orElseGet(() -> {
                Tag created = new Tag();
                created.setName(name);
                created.setUsageCount(0L);
                return created;
            });
            incrementUsage(tag);
            tagRepository.save(tag);
            result.add(tag);
        }

        return result;
    }

    private List<String> normalizeTagNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, String> normalized = new LinkedHashMap<>();
        for (String name : names) {
            if (name == null) {
                continue;
            }
            String trimmed = name.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            normalized.putIfAbsent(trimmed.toLowerCase(), trimmed);
        }
        return new ArrayList<>(normalized.values());
    }

    private Set<Tag> safeTagSet(Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(tags);
    }

    private void incrementUsage(Tag tag) {
        long count = tag.getUsageCount() == null ? 0L : tag.getUsageCount();
        tag.setUsageCount(count + 1L);
    }

    private void decrementUsage(Tag tag) {
        long count = tag.getUsageCount() == null ? 0L : tag.getUsageCount();
        tag.setUsageCount(Math.max(0L, count - 1L));
    }
}

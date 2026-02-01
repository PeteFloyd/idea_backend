package com.learn.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class IdeaServiceTest {

    @Mock
    private IdeaRepository ideaRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private IdeaService ideaService;

    private User user;

    @BeforeEach
    void setUp() {
        user = buildUser(1L, "alice");
    }

    @Test
    void listIdeasEmptyDoesNotQueryLikesWhenUserNull() {
        IdeaQueryRequest request = IdeaQueryRequest.builder().build();
        when(ideaRepository.findAll(any(Specification.class), any(Pageable.class))).thenAnswer(invocation -> {
            Pageable pageable = invocation.getArgument(1);
            return new PageImpl<>(List.of(), pageable, 0);
        });

        PageResponse<IdeaListResponse> response = ideaService.listIdeas(request, null);

        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotalElements());
        verify(likeRepository, never()).findByUserIdAndIdeaIdIn(anyLong(), anyList());
    }

    @Test
    void listIdeasUsesSortByLikeCount() {
        IdeaQueryRequest request = IdeaQueryRequest.builder()
            .sort("likeCount,asc")
            .build();
        when(ideaRepository.findAll(any(Specification.class), any(Pageable.class))).thenAnswer(invocation -> {
            Pageable pageable = invocation.getArgument(1);
            return new PageImpl<>(List.of(), pageable, 0);
        });

        ideaService.listIdeas(request, 9L);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(ideaRepository).findAll(any(Specification.class), captor.capture());
        Pageable pageable = captor.getValue();
        Sort.Order order = pageable.getSort().getOrderFor("likeCount");
        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    void listIdeasMapsLikedFlags() {
        IdeaQueryRequest request = IdeaQueryRequest.builder().build();
        Idea idea1 = buildIdea(1L, user, IdeaStatus.ACTIVE, Set.of());
        Idea idea2 = buildIdea(2L, user, IdeaStatus.ACTIVE, Set.of());
        Page<Idea> page = new PageImpl<>(
            List.of(idea1, idea2),
            PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt")),
            2
        );
        when(ideaRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        Like like = new Like();
        like.setId(7L);
        like.setUser(user);
        like.setIdea(idea2);
        when(likeRepository.findByUserIdAndIdeaIdIn(eq(5L), eq(List.of(1L, 2L))))
            .thenReturn(List.of(like));

        PageResponse<IdeaListResponse> response = ideaService.listIdeas(request, 5L);

        assertEquals(2, response.getContent().size());
        assertFalse(response.getContent().get(0).isLiked());
        assertTrue(response.getContent().get(1).isLiked());
    }

    @Test
    void getIdeaDetailNotFoundThrows404() {
        when(ideaRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> ideaService.getIdeaDetail(99L, 1L));

        assertEquals(404, ex.getCode());
    }

    @Test
    void getIdeaDetailDeletedThrows404() {
        Idea idea = buildIdea(3L, user, IdeaStatus.DELETED, Set.of());
        when(ideaRepository.findById(3L)).thenReturn(Optional.of(idea));

        BusinessException ex = assertThrows(BusinessException.class, () -> ideaService.getIdeaDetail(3L, 1L));

        assertEquals(404, ex.getCode());
    }

    @Test
    void getIdeaDetailReturnsLikedStatus() {
        Idea idea = buildIdea(4L, user, IdeaStatus.ACTIVE, Set.of());
        when(ideaRepository.findById(4L)).thenReturn(Optional.of(idea));
        when(likeRepository.existsByUserIdAndIdeaId(2L, 4L)).thenReturn(true);

        IdeaDetailResponse response = ideaService.getIdeaDetail(4L, 2L);

        assertTrue(response.isLiked());
    }

    @Test
    void createIdeaCreatesNewAndExistingTags() {
        CreateIdeaRequest request = CreateIdeaRequest.builder()
            .title("Title")
            .description("Desc")
            .images(List.of("img1"))
            .tags(Arrays.asList(" Green ", "green", "", null, "Blue"))
            .build();
        Tag green = buildTag(10L, "Green", 2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tagRepository.findByNameIgnoreCase("Green")).thenReturn(Optional.of(green));
        when(tagRepository.findByNameIgnoreCase("Blue")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ideaRepository.save(any(Idea.class))).thenAnswer(invocation -> {
            Idea saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        IdeaDetailResponse response = ideaService.createIdea(request, 1L);

        assertEquals(100L, response.getId());
        ArgumentCaptor<Idea> ideaCaptor = ArgumentCaptor.forClass(Idea.class);
        verify(ideaRepository).save(ideaCaptor.capture());
        Idea saved = ideaCaptor.getValue();
        assertEquals(IdeaStatus.ACTIVE, saved.getStatus());
        assertEquals("Title", saved.getTitle());
        assertEquals(2, saved.getTags().size());
        verify(tagRepository, times(1)).findByNameIgnoreCase("Green");
        verify(tagRepository, times(1)).findByNameIgnoreCase("Blue");

        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository, times(2)).save(tagCaptor.capture());
        List<Tag> savedTags = tagCaptor.getAllValues();
        Tag savedGreen = savedTags.stream().filter(t -> "Green".equals(t.getName())).findFirst().orElse(null);
        Tag savedBlue = savedTags.stream().filter(t -> "Blue".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(savedGreen);
        assertNotNull(savedBlue);
        assertEquals(3L, savedGreen.getUsageCount());
        assertEquals(1L, savedBlue.getUsageCount());
    }

    @Test
    void updateIdeaNonAuthorThrows403() {
        Idea idea = buildIdea(5L, user, IdeaStatus.ACTIVE, Set.of());
        when(ideaRepository.findById(5L)).thenReturn(Optional.of(idea));
        UpdateIdeaRequest request = UpdateIdeaRequest.builder()
            .title("T")
            .description("D")
            .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> ideaService.updateIdea(5L, request, 2L));

        assertEquals(403, ex.getCode());
    }

    @Test
    void updateIdeaDeletedThrows404() {
        Idea idea = buildIdea(6L, user, IdeaStatus.DELETED, Set.of());
        when(ideaRepository.findById(6L)).thenReturn(Optional.of(idea));
        UpdateIdeaRequest request = UpdateIdeaRequest.builder()
            .title("T")
            .description("D")
            .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> ideaService.updateIdea(6L, request, 1L));

        assertEquals(404, ex.getCode());
    }

    @Test
    void updateIdeaSyncsTags() {
        Tag tagA = buildTag(1L, "A", 2L);
        Tag tagB = buildTag(2L, "B", 3L);
        Tag tagC = buildTag(3L, "C", 5L);
        Idea idea = buildIdea(7L, user, IdeaStatus.ACTIVE, new HashSet<>(Set.of(tagA, tagB)));
        when(ideaRepository.findById(7L)).thenReturn(Optional.of(idea));
        when(tagRepository.findByNameIgnoreCase("C")).thenReturn(Optional.of(tagC));
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ideaRepository.save(any(Idea.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UpdateIdeaRequest request = UpdateIdeaRequest.builder()
            .title("New")
            .description("New Desc")
            .tags(List.of("B", "C"))
            .build();

        IdeaDetailResponse response = ideaService.updateIdea(7L, request, 1L);

        assertEquals(2, response.getTags().size());
        assertEquals(1L, tagA.getUsageCount());
        assertEquals(3L, tagB.getUsageCount());
        assertEquals(6L, tagC.getUsageCount());
        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository, times(2)).save(tagCaptor.capture());
        List<Tag> savedTags = tagCaptor.getAllValues();
        assertTrue(savedTags.contains(tagA));
        assertTrue(savedTags.contains(tagC));
    }

    @Test
    void deleteIdeaAuthorSoftDeletesAndDecrementsTags() {
        Tag tagA = buildTag(1L, "A", 1L);
        Tag tagB = buildTag(2L, "B", 0L);
        Idea idea = buildIdea(8L, user, IdeaStatus.ACTIVE, Set.of(tagA, tagB));
        when(ideaRepository.findById(8L)).thenReturn(Optional.of(idea));
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ideaService.deleteIdea(8L, 1L, false);

        assertEquals(IdeaStatus.DELETED, idea.getStatus());
        assertEquals(0L, tagA.getUsageCount());
        assertEquals(0L, tagB.getUsageCount());
        verify(tagRepository, times(2)).save(any(Tag.class));
        verify(ideaRepository).save(idea);
    }

    @Test
    void deleteIdeaNonAuthorNonAdminThrows403() {
        Idea idea = buildIdea(9L, user, IdeaStatus.ACTIVE, Set.of());
        when(ideaRepository.findById(9L)).thenReturn(Optional.of(idea));

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> ideaService.deleteIdea(9L, 2L, false)
        );

        assertEquals(403, ex.getCode());
    }

    @Test
    void deleteIdeaAdminBypassAllowed() {
        Idea idea = buildIdea(10L, user, IdeaStatus.ACTIVE, Set.of());
        when(ideaRepository.findById(10L)).thenReturn(Optional.of(idea));

        ideaService.deleteIdea(10L, 2L, true);

        assertEquals(IdeaStatus.DELETED, idea.getStatus());
        verify(ideaRepository).save(idea);
    }

    @Test
    void getCurrentUserIdeasSetsUserId() {
        IdeaQueryRequest request = IdeaQueryRequest.builder().build();
        when(ideaRepository.findAll(any(Specification.class), any(Pageable.class))).thenAnswer(invocation -> {
            Pageable pageable = invocation.getArgument(1);
            return new PageImpl<>(List.of(), pageable, 0);
        });

        ideaService.getCurrentUserIdeas(request, 42L);

        assertEquals(42L, request.getUserId());
    }

    private User buildUser(Long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setPassword("pw");
        return u;
    }

    private Idea buildIdea(Long id, User owner, IdeaStatus status, Set<Tag> tags) {
        Idea idea = new Idea();
        idea.setId(id);
        idea.setUser(owner);
        idea.setTitle("Title " + id);
        idea.setDescription("Desc " + id);
        idea.setStatus(status);
        idea.setTags(tags);
        return idea;
    }

    private Tag buildTag(Long id, String name, long usageCount) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        tag.setUsageCount(usageCount);
        return tag;
    }
}

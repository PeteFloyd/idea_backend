package com.learn.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.learn.demo.entity.Tag;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Test
    void findByNameIgnoreCaseMatchesRegardlessOfCase() {
        tagRepository.save(buildTag("Java", 5L));

        assertTrue(tagRepository.findByNameIgnoreCase("java").isPresent());
        assertTrue(tagRepository.findByNameIgnoreCase("JAVA").isPresent());
        assertTrue(tagRepository.findByNameIgnoreCase("Java").isPresent());
    }

    @Test
    void findByNameIgnoreCaseReturnsEmptyWhenMissing() {
        assertTrue(tagRepository.findByNameIgnoreCase("missing").isEmpty());
    }

    @Test
    void existsByNameIgnoreCaseMatchesRegardlessOfCase() {
        tagRepository.save(buildTag("Cloud", 1L));

        assertTrue(tagRepository.existsByNameIgnoreCase("cloud"));
        assertFalse(tagRepository.existsByNameIgnoreCase("other"));
    }

    @Test
    void findTop10ByOrderByUsageCountDescReturnsTopTenSorted() {
        for (int i = 1; i <= 12; i++) {
            tagRepository.save(buildTag("tag" + i, (long) i));
        }

        List<Tag> top = tagRepository.findTop10ByOrderByUsageCountDesc();
        assertEquals(10, top.size());
        assertEquals(12L, top.get(0).getUsageCount());
        assertEquals(3L, top.get(9).getUsageCount());
        assertTrue(top.get(0).getUsageCount() >= top.get(1).getUsageCount());
    }

    private Tag buildTag(String name, Long usageCount) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setUsageCount(usageCount);
        return tag;
    }
}

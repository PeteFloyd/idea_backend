package com.learn.demo.repository;

import com.learn.demo.entity.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
	Optional<Tag> findByNameIgnoreCase(String name);

	List<Tag> findTop10ByOrderByUsageCountDesc();

	boolean existsByNameIgnoreCase(String name);
}

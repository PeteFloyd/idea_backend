package com.learn.demo.repository;

import com.learn.demo.entity.Idea;
import com.learn.demo.enums.IdeaStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.domain.Specification;

public interface IdeaRepository
	extends JpaRepository<Idea, Long>, JpaSpecificationExecutor<Idea> {
	Page<Idea> findByUserId(Long userId, Pageable pageable);

	Page<Idea> findByStatus(IdeaStatus status, Pageable pageable);

	Optional<Idea> findByUserIdAndIdAndStatus(Long userId, Long id, IdeaStatus status);

	@Query(
		"select i from Idea i where i.title like %:keyword% or i.description like %:keyword%"
	)
	Page<Idea> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

	@EntityGraph(attributePaths = {"user", "tags"})
	Page<Idea> findAll(Specification<Idea> spec, Pageable pageable);
}

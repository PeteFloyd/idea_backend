package com.learn.demo.repository;

import com.learn.demo.entity.Like;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
	boolean existsByUserIdAndIdeaId(Long userId, Long ideaId);

	long countByIdeaId(Long ideaId);

	Optional<Like> findByUserIdAndIdeaId(Long userId, Long ideaId);

	List<Like> findByUserIdAndIdeaIdIn(Long userId, List<Long> ideaIds);
}

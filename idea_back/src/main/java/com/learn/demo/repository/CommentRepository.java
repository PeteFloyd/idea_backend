package com.learn.demo.repository;

import com.learn.demo.entity.Comment;
import com.learn.demo.enums.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	Page<Comment> findByIdeaIdAndStatus(Long ideaId, CommentStatus status, Pageable pageable);

	long countByIdeaIdAndStatus(Long ideaId, CommentStatus status);
}

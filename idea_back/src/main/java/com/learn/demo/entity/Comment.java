package com.learn.demo.entity;

import com.learn.demo.enums.CommentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@Entity
public class Comment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "idea_id", nullable = false)
	private Idea idea;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Size(max = 500)
	@Column(nullable = false, length = 500)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CommentStatus status;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;
}

package com.learn.demo.entity;

import com.learn.demo.converter.ImagesConverter;
import com.learn.demo.enums.IdeaStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@NoArgsConstructor
@Entity
@Table(name = "ideas")
public class Idea {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Convert(converter = ImagesConverter.class)
	@Column(columnDefinition = "TEXT")
	private List<String> images;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private IdeaStatus status;

	@Column(nullable = false)
	private Long likeCount = 0L;

	@Column(nullable = false)
	private Long commentCount = 0L;

	@ManyToMany
	@JoinTable(
		name = "idea_tags",
		joinColumns = @JoinColumn(name = "idea_id"),
		inverseJoinColumns = @JoinColumn(name = "tag_id")
	)
	private Set<Tag> tags = new HashSet<>();

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private LocalDateTime updatedAt;
}

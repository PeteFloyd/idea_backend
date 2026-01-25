package com.learn.demo.entity;

import com.learn.demo.enums.ReportStatus;
import com.learn.demo.enums.TargetType;
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
public class Report {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "reporter_id", nullable = false)
	private User reporter;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TargetType targetType;

	@Column(nullable = false)
	private Long targetId;

	@Size(max = 200)
	@Column(nullable = false, length = 200)
	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReportStatus status;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;
}

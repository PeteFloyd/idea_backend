package com.learn.demo.repository;

import com.learn.demo.entity.Report;
import com.learn.demo.enums.ReportStatus;
import com.learn.demo.enums.TargetType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
	List<Report> findByTargetTypeAndTargetId(TargetType type, Long targetId);

	Page<Report> findByStatus(ReportStatus status, Pageable pageable);

	long countByStatus(ReportStatus status);
}

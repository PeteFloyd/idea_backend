package com.learn.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.learn.demo.entity.Report;
import com.learn.demo.entity.User;
import com.learn.demo.enums.ReportStatus;
import com.learn.demo.enums.TargetType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByTargetTypeAndTargetIdReturnsMatchingReports() {
        User reporter = userRepository.save(buildUser("reporter", "reporter@example.com"));
        reportRepository.save(buildReport(reporter, TargetType.IDEA, 10L, ReportStatus.PENDING));
        reportRepository.save(buildReport(reporter, TargetType.IDEA, 10L, ReportStatus.RESOLVED));
        reportRepository.save(buildReport(reporter, TargetType.COMMENT, 10L, ReportStatus.PENDING));

        List<Report> reports = reportRepository.findByTargetTypeAndTargetId(TargetType.IDEA, 10L);
        assertEquals(2, reports.size());
        assertTrue(reports.stream().allMatch(report -> report.getTargetType() == TargetType.IDEA));
    }

    @Test
    void findByTargetTypeAndTargetIdReturnsEmptyWhenMissing() {
        List<Report> reports = reportRepository.findByTargetTypeAndTargetId(TargetType.IDEA, 999L);
        assertTrue(reports.isEmpty());
    }

    @Test
    void findByStatusReturnsOnlyMatchingStatus() {
        User reporter = userRepository.save(buildUser("statusr", "statusr@example.com"));
        reportRepository.save(buildReport(reporter, TargetType.IDEA, 1L, ReportStatus.PENDING));
        reportRepository.save(buildReport(reporter, TargetType.IDEA, 2L, ReportStatus.RESOLVED));

        Page<Report> pending = reportRepository.findByStatus(ReportStatus.PENDING, PageRequest.of(0, 10));
        assertEquals(1, pending.getTotalElements());
        assertEquals(ReportStatus.PENDING, pending.getContent().get(0).getStatus());
    }

    @Test
    void findByStatusReturnsEmptyWhenMissing() {
        Page<Report> pending = reportRepository.findByStatus(ReportStatus.PENDING, PageRequest.of(0, 10));
        assertEquals(0, pending.getTotalElements());
    }

    @Test
    void countByStatusCountsMatching() {
        User reporter = userRepository.save(buildUser("countrep", "countrep@example.com"));
        reportRepository.save(buildReport(reporter, TargetType.IDEA, 1L, ReportStatus.PENDING));
        reportRepository.save(buildReport(reporter, TargetType.COMMENT, 2L, ReportStatus.PENDING));
        reportRepository.save(buildReport(reporter, TargetType.COMMENT, 3L, ReportStatus.RESOLVED));

        assertEquals(2, reportRepository.countByStatus(ReportStatus.PENDING));
        assertEquals(1, reportRepository.countByStatus(ReportStatus.RESOLVED));
    }

    @Test
    void countByStatusReturnsZeroWhenMissing() {
        assertEquals(0, reportRepository.countByStatus(ReportStatus.REJECTED));
    }

    private User buildUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("secret123");
        user.setEmail(email);
        return user;
    }

    private Report buildReport(User reporter, TargetType type, Long targetId, ReportStatus status) {
        Report report = new Report();
        report.setReporter(reporter);
        report.setTargetType(type);
        report.setTargetId(targetId);
        report.setReason("reason");
        report.setStatus(status);
        return report;
    }
}

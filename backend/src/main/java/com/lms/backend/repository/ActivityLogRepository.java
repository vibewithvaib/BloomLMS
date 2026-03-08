package com.lms.backend.repository;

import com.lms.backend.model.ActivityLog;
import com.lms.backend.model.Course;
import com.lms.backend.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByUserOrderByCreatedAtDesc(User user);
    List<ActivityLog> findByCourseOrderByCreatedAtDesc(Course course);
}

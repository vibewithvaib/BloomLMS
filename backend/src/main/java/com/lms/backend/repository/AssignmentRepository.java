package com.lms.backend.repository;

import com.lms.backend.model.Assignment;
import com.lms.backend.model.Course;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourse(Course course);
}

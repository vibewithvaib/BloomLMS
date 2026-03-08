package com.lms.backend.repository;

import com.lms.backend.model.Course;
import com.lms.backend.model.CourseStep;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseStepRepository extends JpaRepository<CourseStep, Long> {
    List<CourseStep> findByCourseOrderByStepOrderAsc(Course course);
}

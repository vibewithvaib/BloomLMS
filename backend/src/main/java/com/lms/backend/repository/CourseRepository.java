package com.lms.backend.repository;

import com.lms.backend.model.Course;
import com.lms.backend.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructor(User instructor);
    List<Course> findByPublishedTrue();
}

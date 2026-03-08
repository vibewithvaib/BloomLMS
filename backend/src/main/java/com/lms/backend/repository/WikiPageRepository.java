package com.lms.backend.repository;

import com.lms.backend.model.Course;
import com.lms.backend.model.WikiPage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WikiPageRepository extends JpaRepository<WikiPage, Long> {
    List<WikiPage> findByCourse(Course course);
}
